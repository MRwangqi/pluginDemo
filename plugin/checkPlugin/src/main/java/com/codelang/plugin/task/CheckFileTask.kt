package com.codelang.plugin.task

import com.codelang.plugin.check.base.BaseFileCheck
import com.codelang.plugin.html.IndexHtml
import org.gradle.api.Project
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/23.
 */
class CheckFileTask(private val project: Project) {

    fun runTask(taskName: String, configurationName: String, checkFiles: ArrayList<BaseFileCheck>) {
        project.tasks.create(taskName) {
            it.doLast {
                //
                project.configurations.getByName(configurationName).resolvedConfiguration.resolvedArtifacts.forEach {
                    // 版本
                    println(it.moduleVersion.toString()+" --> "+it.file)
                }


                val list = project.configurations.getByName(configurationName).asPath.split(File.pathSeparator)
                        .toList()

                IndexHtml.cleanSection()

                val startTime = System.currentTimeMillis()

                checkFiles.forEach {
                    it.onStart()
                }

                list.forEach { path ->
                    val input = FileInputStream(path)
                    val zipInputStream = ZipInputStream(BufferedInputStream(input))
                    var ze: ZipEntry?
                    while (zipInputStream.nextEntry.also { ze = it } != null) {
                        if (ze!!.isDirectory) {
                            continue
                        }
                        checkFiles.forEach { fileCheck ->
                            val lastIndex = path.lastIndexOf("/") + 1
                            fileCheck.onIteratorFile(path.substring(0, lastIndex), path.substring(lastIndex), ze!!.name, ze!!.size, zipInputStream)
                        }
                    }
                    zipInputStream.closeEntry()
                    input.close()
                }

                checkFiles.forEach {
                    it.onEnd()
                }

                val endTime = System.currentTimeMillis()
                println("耗时:" + ((endTime - startTime) / 1000f) + "s")

                generatorHtml()
            }
        }
    }

    private fun generatorHtml() {
        val buildDir = File(project.buildDir.absolutePath)
        if (!buildDir.exists()) {
            buildDir.mkdir()
        }

        // 写文件
        val tempDir = File(project.buildDir.absolutePath + File.separator + "checkPlugin")
        if (!tempDir.exists()) {
            tempDir.mkdir()
        }

        val htmlFile = File(tempDir, "check.html")
        if (htmlFile.exists()) {
            htmlFile.delete()
        }

        htmlFile.createNewFile()
        htmlFile.writeText(IndexHtml.getHtml())

        println("report file:" + htmlFile.absolutePath)
    }

}