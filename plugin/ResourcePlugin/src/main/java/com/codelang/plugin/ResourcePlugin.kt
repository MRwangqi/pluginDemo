package com.codelang.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import org.gradle.api.logging.Logger


class ResourcePlugin : Plugin<Project> {

    var logger: Logger? = null
    override fun apply(project: Project) {
        println("ResourcePlugin---------->")
        val appExtension = project.extensions.getByType(AppExtension::class.java)

        logger = project.logger

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
            println()
            logger?.error("-------- layout 资源重复----------")
            duplicateLayout.forEach {
                println(it.key)
                it.value.forEach {
                    logger?.error("----> $it")
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
                    // Drawable 是否重复只判断文件名，不判断扩展名，因为 XX.png,xx.jpeg,xx.webp 都是指一个图片
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
            println()
            logger?.error("-------- Drawable 资源重复----------")
            duplicateDrawable.forEach {
                println(it.key)
                it.value.forEach {
                    logger?.error("----> $it")
                }
            }
        }
    }

    private fun checkAssets(libraryAssets: Set<File>?, sourceFolderAssets: Set<File>) {

        val files = arrayListOf<File>()
        files.addAll(libraryAssets?.toList() ?: arrayListOf())
        files.addAll(sourceFolderAssets.toList())

        val duplicateAssets = HashMap<String, ArrayList<String>>()
        val hashMap = HashMap<String, String>()

        //  递归遍历 assets 下的文件，以相对路径为 key，然后遍历重复 file
        files.forEach { assets ->
            val list = arrayListOf<File>().apply {
                getFiles(assets, this)
            }

            list.forEach {
                val relativePath = it.absolutePath.replace(assets.absolutePath, "")
                if (hashMap.containsKey(relativePath) && relativePath.isNotEmpty()) {
                    // assets 资源重复
                    var l = duplicateAssets[relativePath]
                    if (l == null) {
                        l = arrayListOf()
                        l.add(hashMap[relativePath] ?: "")
                    }
                    l.add(it.absolutePath)
                    duplicateAssets[relativePath] = l
                } else {
                    hashMap[relativePath] = it.absolutePath
                }
            }
        }

        if (duplicateAssets.isNotEmpty()) {
            println()
            logger?.error("-------- assets 资源重复----------")
            duplicateAssets.forEach {
                println(it.key)
                it.value.forEach {
                    logger?.error("----> $it")
                }
            }
        }
    }

    private fun getFiles(file: File, list: ArrayList<File>) {
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                getFiles(it, list)
            }
        } else {
            list.add(file)
        }
    }

}