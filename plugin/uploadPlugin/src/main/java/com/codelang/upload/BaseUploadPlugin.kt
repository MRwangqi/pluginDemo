package com.codelang.upload

import com.codelang.upload.config.UploadConfig
import com.codelang.upload.task.androidSourcesJar
import com.codelang.upload.task.emptySourcesJar
import com.codelang.upload.task.javaSourcesJar
import com.codelang.upload.utils.Util
import groovy.util.Node
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import java.io.File
import java.io.InputStreamReader
import java.net.URI

/**
 * @author wangqi
 * @since 2022/3/20.
 */
abstract class BaseUploadPlugin : Plugin<Project> {

    final override fun apply(project: Project) {
        if (Util.isApplication(project)) {
            return
        }

        if (!project.plugins.hasPlugin("maven-publish")) {
            project.plugins.apply("maven-publish")
        }
        project.extensions.create("upload", UploadConfig::class.java)


        project.afterEvaluate {
            val uploadConfig = project.extensions.findByName("upload") as UploadConfig

            if (uploadConfig.groupId.isEmpty() || uploadConfig.artifactId.isEmpty() || uploadConfig.version.isEmpty()) {
                println("upload 配置的 GAV 有空值:")
                println("groupId=${uploadConfig.groupId}")
                println("artifactId=${uploadConfig.artifactId}")
                println("version=${uploadConfig.version}")
                return@afterEvaluate
            }


            if (!isSupportUpload(uploadConfig)) {
                return@afterEvaluate
            }

            val localRepo = project.file("../build/repo")
            var mavenUrl = ""

            val publishingExtension = Util.publishingExtension(project)
            publishingExtension?.publications {
                it.register(
                        "maven",
                        MavenPublication::class.java
                ) { publication ->

                    if (Util.isAndroidModule(project)) {
                        publication.from(project.components.findByName("release"))
                    } else {
                        publication.from(project.components.findByName("java"))
                    }

                    publication.groupId = uploadConfig.groupId
                    publication.artifactId = uploadConfig.artifactId
                    publication.version = uploadConfig.version

                    publication.artifact(addSourceJar(uploadConfig.sourceJar, project))

                    //pom config
                    publication.pom { pom ->
                        addDeveloper(pom)
                        if (!uploadConfig.hasPomDepend) {
                            removePomDeps(pom)
                        } else {
                            addPomDeps(pom, project)
                        }
                    }
                }
            }


            publishingExtension?.repositories {
                it.maven { repo ->
                    if (uploadConfig.nexusURL.startsWith("http")) {
                        // 反射设置当前允许 http 不安全协议
                        val method = repo::class.java.methods.firstOrNull {
                            it.name == "setAllowInsecureProtocol"
                        }
                        method?.invoke(repo, true)
                    }

                    if (isCredentials(uploadConfig)) {
                        mavenUrl = uploadConfig.nexusURL
                        repo.url = URI.create(uploadConfig.nexusURL)
                        repo.credentials { credential ->
                            credential.username = uploadConfig.nexusName
                            credential.password = uploadConfig.nexusPsw
                        }
                    } else {
                        mavenUrl = localRepo.absolutePath
                        repo.url = localRepo.toURI()
                    }
                }
            }

            project.task("upload") {
                it.dependsOn("publishMavenPublicationToMavenRepository")
            }

            project.tasks.firstOrNull {
                it.name == "publishMavenPublicationToMavenRepository"
            }?.doLast {
                uploadComplete(uploadConfig, mavenUrl, project)
            }
        }
    }


    abstract fun isSupportUpload(uploadConfig: UploadConfig): Boolean


    abstract fun isCredentials(uploadConfig: UploadConfig):Boolean


    abstract fun uploadComplete(uploadConfig: UploadConfig, mavenUrl: String, project: Project)


    private fun addPomDeps(pom: MavenPom, project: Project) {
        val scopeMapping = mapOf<String, String?>(
                "api" to "compile",
                "implementation" to "compile",
                "runtimeOnly" to "runtime",
                "compileOnly" to "provided",
        )
        pom.withXml { xml ->

            var dependenciesNode: Node? = xml.asNode().children().firstOrNull {
                (it is Node) && it.name().toString().contains("dependencies")
            } as? Node

            if (dependenciesNode == null) {
                dependenciesNode = xml.asNode().appendNode("dependencies")
            }

            scopeMapping.keys.forEach { key ->
                project.configurations.getByName(key).dependencies.forEach { dependency ->

                    println("---> " + dependency)

                    if (dependency is DefaultProjectDependency) {
                        val buildGradle = File(dependency.dependencyProject.projectDir, "build.gradle")
                        // todo 正则匹配拿到 upload 的 GAV
                        println(buildGradle.readText())

                    } else {
                        val dependencyNode = dependenciesNode?.appendNode("dependency")
                        dependencyNode?.appendNode("groupId", dependency.group)
                        dependencyNode?.appendNode("artifactId", dependency.name)
                        dependencyNode?.appendNode("version", dependency.version)
                        dependencyNode?.appendNode("scope", scopeMapping[key])
                    }
                }
            }
        }
    }

    private fun removePomDeps(pom: MavenPom) {
        pom.withXml {
            val root = it.asNode()
            val dependenciesNode = root.children().firstOrNull {
                (it is Node) && it.name().toString().contains("dependencies")
            }
            if (dependenciesNode != null) {
                root.remove(dependenciesNode as Node)
            }
        }
    }

    private fun addDeveloper(pom: MavenPom) {
        pom.developers {
            it.developer {
                val p = Runtime.getRuntime().exec("git config user.email")
                p.waitFor()
                if (p.exitValue() == 0) {
                    it.email.set(InputStreamReader(p.inputStream).readText().trim())
                }

            }
        }
    }

    private fun addSourceJar(sourceJar: Boolean, project: Project): Task {
        if (!sourceJar) {
            return project.emptySourcesJar()
        }
        return if (Util.isAndroidModule(project)) {
            project.androidSourcesJar()
        } else {
            project.javaSourcesJar()
        }
    }
}