package com.codelang.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

// https://nunu03.github.io/2021/09/06/Gradle%E4%B9%8Bandroid.precompileDependenciesResources%E9%85%8D%E7%BD%AE%E4%BB%8B%E7%BB%8D/
class ResourcePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("ResourcePlugin---------->")
        val appExtension = project.extensions.getByType(AppExtension::class.java)

        appExtension.applicationVariants.all { variant ->
            val mergeResourcesTask = variant.mergeResourcesProvider.get()
            val mergeAssetsTask = variant.mergeAssetsProvider.get()

            val resourceTask = project.task("DumplicateResource${variant.name.capitalize()}Check")
            resourceTask.doLast {
                // 获取所有参与合并的 res 资源
                val files = variant.allRawAndroidResources.files
                checkResLayout(files)
                checkResDrawable(files)
            }
            mergeResourcesTask.dependsOn(resourceTask)
        }
    }

    /**
     * 检查 Layout 资源是否冲突
     */
    private fun checkResLayout(files: Set<File>) {
        val hashMap = HashMap<String, String>()
        val dumplicateLayout = HashMap<String, Pair<String, String>>()
        files.forEach { file ->
            val layoutDir = file.listFiles()?.firstOrNull { it.isDirectory && it.name == "layout" }
            if (layoutDir != null) {
                layoutDir.listFiles()?.forEach { layout ->
                    if (hashMap.containsKey(layout.name)) {
                        // 资源重复
                        dumplicateLayout[layout.name] = Pair(layout.absolutePath, hashMap[layout.name]
                                ?: "")
                    } else {
                        hashMap[layout.name] = layout.absolutePath
                    }
                }
            }
        }

        if (dumplicateLayout.isNotEmpty()) {
            println("-------- layout 资源重复----------")
            dumplicateLayout.forEach {
                println(it.key + " ---- " + it.value)
            }
        }
    }

    /**
     * 检查 Drawable 资源是否冲突
     */
    private fun checkResDrawable(files: Set<File>) {
        val hashMap = HashMap<String, String>()
        val dumplicateDrawable = HashMap<String, Pair<String, String>>()
        files.forEach { file ->
            val drawableDir = file.listFiles()?.filter { it.isDirectory && it.name.contains("drawable") }

            // drawable 有多个目录，同一个模块下如果出现多个资源名相同的话，只需记录一个即可
            val moduleMap = HashMap<String, String>()
            drawableDir?.map { drawable ->
                drawable.listFiles()?.forEach { file ->
                    if (hashMap.containsKey(file.name)) {
                        // 资源重复
                        dumplicateDrawable[file.name] = Pair(file.absolutePath, hashMap[file.name]
                                ?: "")
                    } else {
                        moduleMap[file.name] = file.absolutePath
                    }
                }
            }

            hashMap.putAll(moduleMap)
        }

        if (dumplicateDrawable.isNotEmpty()) {
            println("-------- Drawable 资源重复----------")
            dumplicateDrawable.forEach {
                println(it.key + " ---- " + it.value)
            }
        }
    }
}