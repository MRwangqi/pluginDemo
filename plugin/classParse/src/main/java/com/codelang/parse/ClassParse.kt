package com.codelang.parse

import classfile.ClassFile
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.jar.JarFile

class ClassParse : Plugin<Project> {

    companion object {
        const val BUILD = "build"
    }

    override fun apply(project: Project) {
        // ./gradlew checkDependency -Pbuild=fullDebug
        val params = project.gradle.startParameter.projectProperties

        val build = if (params.containsKey(BUILD)) {
            params[BUILD] ?: "debug"
        } else {
            // 默认 debug 兜底
            "debug"
        }

        project.afterEvaluate {
            val configurationName = build + "CompileClasspath"
            project.tasks.create("classParse") {
                it.doLast {
                    val resolvableDeps = project.configurations.getByName(configurationName).incoming
                    val view = resolvableDeps.artifactView { conf ->
                        conf.attributes { attr ->
                            attr.attribute(AndroidArtifacts.ARTIFACT_TYPE, AndroidArtifacts.ArtifactType.CLASSES_JAR.type)
                        }
                    }

                    // 获取依赖 aar transform 的 jar  路径
                    view.artifacts.forEach { result ->
                        val dep = result.variant.displayName
                        val file = result.file
                        unzipJar(dep, file)
                    }
                }
            }
        }
    }


    private fun unzipJar(dep: String, file: File) {
        println("dep=" + dep + " path=" + file.absolutePath)
        // 获取 jar 中的 class 文件
        val jarFile = JarFile(file, false, JarFile.OPEN_READ)
        val jarEntries = jarFile.entries()
        while (jarEntries.hasMoreElements()) {
            val entry = jarEntries.nextElement()
            if (!entry.isDirectory && entry.name.endsWith(".class") && !entry.name.endsWith("module-info.class")) {
                var ins: InputStream? = null
                try {
                    ins = jarFile.getInputStream(entry)
                    val cf = ClassFile.read(ins)
                    parseClass(dep, cf)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (ins != null) {
                        try {
                            ins.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }


    private fun parseClass(dep: String, classFile: ClassFile) {
        println(classFile.name)

        classFile.methods.forEach {

        }
    }
}