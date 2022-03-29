package com.codelang.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class ModulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            val gavMap = hashMapOf<String, Project>()
            project.rootProject.allprojects {
                // 只处理非 application module
                if (!it.plugins.hasPlugin("com.android.application")) {
                    val buildGradleFile = File(it.projectDir, "build.gradle")
                    val gav = getGAV(buildGradleFile.readText())
                    if (gav != null) {
                        gavMap[gav.groupId + gav.artifactId] = it
                    }
                }
            }

            // 为空不处理
            if (gavMap.isEmpty()) {
                return@afterEvaluate
            }

            val configurationName = arrayListOf<String>().apply {
                add("api")
                add("implementation")
                add("runtimeOnly")
                add("compileOnly")
            }

            project.extensions.getByType(AppExtension::class.java).productFlavors.forEach {
                val productFlavor = it::class.java.getMethod("getName").invoke(it)
                configurationName.add("${productFlavor}Api")
                configurationName.add("${productFlavor}Implementation")
                configurationName.add("${productFlavor}RuntimeOnly")
                configurationName.add("${productFlavor}CompileOnly")
            }


            configurationName.map { project.configurations.findByName(it) }
                    .filterNotNull()
                    .forEach { configuration ->
                        configuration.dependencies.filterNotNull().forEach {
                            val p = gavMap[it.group + it.name]
                            if (p != null && !it.group.isNullOrEmpty() && !it.name.isNullOrEmpty()) {
                                // 从主工程中排除 aar 依赖
                                val excludeMap = hashMapOf<String, String>().apply {
                                    put("group", it.group!!)
                                    put("module", it.name)
                                }
                                configuration.exclude(excludeMap)
                                println("排除依赖:${configuration.name} ${it.group}:${it.name}:${it.version}")
                                // 主工程添加 module 模块依赖
                                project.dependencies.add(configuration.name, p)

                                println("替换本地模块:${configuration.name} project(:${p.name})")
                            }
                        }
                    }
        }
    }


    private fun getGAV(text: String): GAV? {
        var groupId: String? = null
        var artifactId: String? = null
        var version: String? = null
        Regex("upload\\s+\\{.+?groupId\\s*=\\s*\"(.+?)\".+?\\}",
                RegexOption.DOT_MATCHES_ALL)
                .find(text)?.groupValues?.let {
                    groupId = it[1]
                }

        Regex("upload\\s+\\{.+?artifactId\\s*=\\s*\"(.+?)\".+?\\}",
                RegexOption.DOT_MATCHES_ALL)
                .find(text)?.groupValues?.let {
                    artifactId = it[1]
                }

        Regex("upload\\s+\\{.+?version\\s*=\\s*\"(.+?)\".+?\\}",
                RegexOption.DOT_MATCHES_ALL)
                .find(text)?.groupValues?.let {
                    version = it[1]
                }

        if (groupId.isNullOrEmpty() || artifactId.isNullOrEmpty() || version.isNullOrEmpty()) {
            return null
        }

        return GAV(groupId!!, artifactId!!, version!!)
    }

    data class GAV(val groupId: String, val artifactId: String, val version: String)
}