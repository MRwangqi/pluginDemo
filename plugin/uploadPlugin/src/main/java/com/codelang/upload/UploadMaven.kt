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
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import java.io.InputStreamReader
import java.net.URI

class UploadMaven : Plugin<Project> {

    override fun apply(project: Project) {
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

            if (uploadConfig.nexusURL.isEmpty() || uploadConfig.nexusName.isEmpty() || uploadConfig.nexusPsw.isEmpty()) {
                println("upload 配置的 nexus 有空值:")
                println("nexusURL=${uploadConfig.nexusURL}")
                println("nexusName=${uploadConfig.nexusName}")
                println("nexusPsw=${uploadConfig.nexusPsw}")
                return@afterEvaluate
            }


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
                        }
                    }
                }
            }


            publishingExtension?.repositories {
                it.maven { repo ->
                    repo.url = URI.create(uploadConfig.nexusURL)
                    repo.credentials { credential ->
                        credential.username = uploadConfig.nexusName
                        credential.password = uploadConfig.nexusPsw
                    }

                }
            }

            project.task("upload") {
                it.dependsOn("publishMavenPublicationToMavenRepository")
            }

            project.tasks.firstOrNull {
                it.name == "publishMavenPublicationToMavenRepository"
            }?.doLast {
                println(
                        """可添加依赖使用:
                           implementation '${uploadConfig.groupId}:${uploadConfig.artifactId}:${uploadConfig.version}' 
                """.trimIndent())
            }
        }
    }


    private fun removePomDeps(pom: MavenPom) {
        pom.withXml {
            val root = it.asNode()
            val dependenciesNode = root.children().filter {
                (it is Node) && it.name().toString().contains("dependencies")
            }.firstOrNull()
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