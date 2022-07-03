package com.codelang.plugin

import com.android.build.gradle.AppExtension
import groovy.util.Node
import groovy.xml.XmlParser
import groovy.xml.XmlUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * 官方清单文件合并规则：
 * https://developer.android.com/studio/build/manifest-merge?hl=zh-cn
 */
class PermissionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        println("PermissionPlugin start ---------->")


        // 获取 main 下的清单文件,如果找不到，则手动指定清单文件
        val mainAndroidManifest = project.extensions.getByType(AppExtension::class.java)
                .sourceSets.find { it.name == "main" }?.manifest?.srcFile
                ?: File(project.projectDir, "src/main/AndroidManifest.xml")

        // 1、读取主工程下清单文件的权限，并从主工程中删除，避免有小伙伴在该文件中提交敏感权限
        val parentNode = XmlParser(false, false).parse(mainAndroidManifest)
        val childrenNode = parentNode.children()
        val permissionNodes = childrenNode.map { it as Node }.filter { it.name() == "uses-permission" }.toList()
        if (permissionNodes.isNotEmpty()) {
            childrenNode.removeAll(permissionNodes)
            val xmlText = XmlUtil.serialize(parentNode)
            mainAndroidManifest.writeText(xmlText)
        }


        // 2、生成权限文件，直接使用主工程清单文件去除权限的模板
        val permissionPath = File(project.projectDir, "permission")
        permissionPath.mkdir()
        val permissionFile = File(permissionPath, "AndroidManifest.xml")
        if (permissionFile.exists()) {
            permissionFile.delete()
        }
        permissionFile.createNewFile()

        // 添加自定义权限
        getPermission().forEach {
            childrenNode.add(0, Node(null, it))
        }
        val xmlText2 = XmlUtil.serialize(parentNode)
        permissionFile.writeText(xmlText2)

        // 将生成的权限文件添加进 main sourceSet 中参与项目编译
        project.afterEvaluate {
            project.extensions.getByType(AppExtension::class.java)
                    .sourceSets.find { it.name == "main" }?.manifest?.srcFile(permissionFile)
        }
    }


    private fun getPermission(): List<String> {
        val list = arrayListOf<String>()
        list.addAll(getCommonPermissions())
        list.addAll(getPhoneBlackPermission())
        return list
    }

    /**
     * 设置通用权限
     */
    private fun getCommonPermissions(): List<String> {
        val list = arrayListOf<String>()
        list.add(Utils.getPermission("android.permission.INTERNET"))
        return list
    }

    /**
     * phone 相关的敏感权限需要移除
     */
    private fun getPhoneBlackPermission(): List<String> {
        val list = arrayListOf<String>()
        list.add(Utils.getRemovePermission("android.permission.CALL_PHONE"))
        list.add(Utils.getRemovePermission("android.permission.READ_PHONE_STATE"))
        return list
    }

}