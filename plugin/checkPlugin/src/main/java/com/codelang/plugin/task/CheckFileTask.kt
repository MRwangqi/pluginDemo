package com.codelang.plugin.task

import com.codelang.plugin.check.base.BaseFileCheck
import org.gradle.api.Project
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/23.
 */
class CheckFileTask(private val project: Project) {

    fun runTask(taskName:String,configurationName: String, checkFiles: ArrayList<BaseFileCheck>) {
        project.tasks.create(taskName) {
            it.doLast {
                val list = project.configurations.getByName(configurationName).asPath.split(File.pathSeparator)
                .toList()


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
                            fileCheck.onIteratorFile(path,path.substring( path.lastIndexOf("/")+1),ze!!.name, ze!!.size, zipInputStream)
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
            }
        }
    }

}