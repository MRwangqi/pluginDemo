package com.codelang.plugin

import com.codelang.plugin.check.so.base.SoFileCheck
import com.codelang.plugin.check.base.BaseFileCheck
import com.codelang.plugin.check.manifest.base.ManifestCheck
import com.codelang.plugin.config.Config
import com.codelang.plugin.extension.CheckExtension
import com.codelang.plugin.task.CheckFileTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.file.File
import java.util.*

class CheckPlugin : Plugin<Project> {
    companion object {
        const val EXT_NAME = "check"
        const val BUILD = "build"
    }

    override fun apply(project: Project) {
        // ./gradlew checkDependency -Pbuild=fullDebug
        val params = project.gradle.startParameter.projectProperties

        if (params.containsKey(BUILD)) {
            Config.build = params[BUILD] ?: "debug"
        } else {
            // 默认 debug 兜底
            Config.build = "debug"
        }

        project.extensions.create(EXT_NAME, CheckExtension::class.java)

        project.afterEvaluate {
            val extension = project.extensions.findByName(EXT_NAME) as CheckExtension

            extension.apply {
                Config.manifestWhiteFile = project.projectDir.absolutePath + File.Companion.separator + manifestWhiteFile
            }

            val checkList = arrayListOf<BaseFileCheck>().apply {
                add(SoFileCheck())
                add(ManifestCheck())
            }

            val configurationName = Config.build + "CompileClasspath"
            val taskName = "checkDependency"

            CheckFileTask(project).runTask(taskName, configurationName, checkList)
        }
    }
}