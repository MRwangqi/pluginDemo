package com.codelang.plugin.check.manifest

import com.codelang.plugin.check.manifest.base.IManifest
import com.codelang.plugin.config.Config
import groovy.util.Node
import org.jetbrains.kotlin.com.google.gson.Gson
import java.io.File
import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/28.
 */
class PermissionManifest : IManifest {
    private val hashMap = HashMap<String, ArrayList<String>>()

    override fun onNode(parentNode: Node, path: String, dependency: String, fileName: String, fileSize: Long, zipInputStream: ZipInputStream) {
        parentNode.children()?.forEach {
            val node = (it as? Node)
            if (node != null && node.name().equals("uses-permission")) {

                val permission = node.attribute("android:name")?.toString() ?: return

                // 白名单没有配置的需要记录输出
                if (!ManifestConfig.permissions.contains(permission)) {
                    var deps = hashMap[dependency]
                    if (deps == null) {
                        deps = ArrayList()
                        hashMap[dependency] = deps
                    }
                    deps.add(permission)
                }
            }
        }
    }

    override fun onEnd() {
        println()
        println("==================== 未匹配的权限 ============================")
        hashMap.forEach { (t, u) ->
            if (u.isNotEmpty()) {
                println("$t :")
                u.forEach {
                    println("---> $it")
                }
            }
        }
    }
}