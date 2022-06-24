package com.codelang.plugin

import com.android.build.gradle.AppExtension
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

            val resourceTask = project.task("DuplicateResource${variant.name.capitalize()}Check")
            resourceTask.doLast {
                // 获取所有参与合并的 res 资源
                val files = variant.allRawAndroidResources.files
                checkResLayout(files)
                checkResDrawable(files)
                checkAssets(mergeAssetsTask.getLibraries()?.files, mergeAssetsTask.sourceFolderInputs.files)
            }
            mergeResourcesTask.dependsOn(resourceTask, mergeAssetsTask)
        }
    }

    /**
     * 检查 Layout 资源是否冲突
     */
    private fun checkResLayout(files: Set<File>) {
        val hashMap = HashMap<String, String>()
        val duplicateLayout = HashMap<String, ArrayList<String>>()
        files.forEach { file ->
            val layoutDir = file.listFiles()?.firstOrNull { it.isDirectory && it.name == "layout" }
            if (layoutDir != null) {
                layoutDir.listFiles()?.forEach { layout ->
                    if (hashMap.containsKey(layout.name)) {
                        var list = duplicateLayout[layout.name]
                        if (list == null) {
                            list = arrayListOf()
                            list.add(hashMap[layout.name] ?: "")
                        }
                        list.add(layout.absolutePath)
                        // 资源重复
                        duplicateLayout[layout.name] = list
                    } else {
                        hashMap[layout.name] = layout.absolutePath
                    }
                }
            }
        }

        if (duplicateLayout.isNotEmpty()) {
            println("-------- layout 资源重复----------")
            duplicateLayout.forEach {
                println(it.key)
                it.value.forEach {
                    println("----> $it")
                }
            }
        }
    }

    /**
     * 检查 Drawable 资源是否冲突
     */
    private fun checkResDrawable(files: Set<File>) {
        val hashMap = HashMap<String, ArrayList<String>>()
        val duplicateDrawable = HashMap<String, ArrayList<String>>()
        files.forEach { file ->
            val drawableDir = file.listFiles()?.filter { it.isDirectory && it.name.contains("drawable") }

            val moduleMap = HashMap<String, ArrayList<String>>()
            drawableDir?.map { drawable ->
                drawable.listFiles()?.forEach { file ->
                    // Drawable 是否重复只判断文件名，不判断扩展名，为因为 XX.png,xx.jpeg,xx.webp 都是指一个图片
                    if (hashMap.containsKey(file.nameWithoutExtension)) {
                        // 资源重复
                        var list = duplicateDrawable[file.nameWithoutExtension]
                        if (list == null) {
                            list = arrayListOf()
                            list.addAll(hashMap[file.nameWithoutExtension] ?: arrayListOf())
                        }
                        list.add(file.absolutePath)
                        duplicateDrawable[file.nameWithoutExtension] = list
                    } else {
                        val list = moduleMap[file.nameWithoutExtension] ?: arrayListOf()
                        list.add(file.absolutePath)
                        moduleMap[file.nameWithoutExtension] = list
                    }
                }
            }

            hashMap.putAll(moduleMap)
        }

        if (duplicateDrawable.isNotEmpty()) {
            println("-------- Drawable 资源重复----------")
            duplicateDrawable.forEach {
                println(it.key)
                it.value.forEach {
                    println("----> $it")
                }
            }
        }
    }

    private fun checkAssets(libraryAssets: Set<File>?, sourceFolderAssets: Set<File>) {

        val files = arrayListOf<File>()
        files.addAll(libraryAssets?.toList() ?: arrayListOf())
        files.addAll(sourceFolderAssets.toList())

        // todo 得递归遍历 assets 下的路径，以相对路径为 key，然后遍历重复 file ，待做


        val hashMap = HashMap<String, String>()
        val duplicateAssets = HashMap<String, ArrayList<String>>()
        files.forEach { file ->
//            val assetsDir = file.listFiles()?.firstOrNull { it.isDirectory }
//            if (assetsDir != null) {
            file.listFiles()?.forEach { layout ->
                println("----->"+layout.absolutePath)
                    if (hashMap.containsKey(layout.name)) {
                        var list = duplicateAssets[layout.name]
                        if (list == null) {
                            list = arrayListOf()
                            list.add(hashMap[layout.name] ?: "")
                        }
                        list.add(layout.absolutePath)
                        // 资源重复
                        duplicateAssets[layout.name] = list
                    } else {
                        hashMap[layout.name] = layout.absolutePath
                    }
                }
//            }
        }

        if (duplicateAssets.isNotEmpty()) {
            println("-------- assets 资源重复----------")
            duplicateAssets.forEach {
                println(it.key)
                it.value.forEach {
                    println("----> $it")
                }
            }
        }
    }


}