package com.codelang.upload

import com.android.build.gradle.LibraryExtension
import com.codelang.upload.utils.Util
import groovy.util.Node
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.io.InputStreamReader

class UploadGithub : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin("maven-publish")) {
            project.plugins.apply("maven-publish")
        }
//        val publishingConfig = project.extensions.create("publishConfig", PublishConfig::class.java)

        project.afterEvaluate {
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

                    publication.groupId = "com.aa.bb"
                    publication.artifactId = "haha"
                    publication.version = "1.0.0"
                    publication.artifact(addSourceJar(project))

                    //pom config
                    publication.pom { pom ->
                        addDeveloper(pom)

                        // todo AGP 7.0 的实践效果看，默认就会把 dependencies 下的依赖打入了 pom
                        // todo 所以，这个地方改成，如果 hasPom 为 false 的话，则移除 dependencies
//                        applyPomDeps(pom = pom, project = project)
                        removePomDeps(pom)
                    }
                }
            }

            publishingExtension?.repositories {
                it.maven { repo ->
                    repo.url = project.file("../build/repo").toURI()
                }
            }

            project.task("upload") {
                it.dependsOn("publishMavenPublicationToMavenRepository")
            }

            project.tasks.filter {
                it.name == "publishMavenPublicationToMavenRepository"
            }.firstOrNull()?.doLast {
                println("\n可添加依赖使用：\timplementation '${"GroupId"}:${"SdkName"}:${"SdkVersion"}'\n")
            }
        }

    }


    private val scopeMapping = mapOf<String, String?>(
            "api" to "compile",
            "implementation" to "compile",
            "compile" to "compile"
    )

    private fun applyPomDeps(pom: MavenPom, project: Project) {
        pom.withXml { xml ->
            val dependenciesNode = xml.asNode().appendNode("dependencies")
            //Iterate over the compile dependencies (we don't want the test ones), adding a <dependency> node for each
            scopeMapping.keys.forEach { key ->
                try {
                    project.configurations.getByName(key).allDependencies?.forEach { dependency ->
                        val dependencyNode = dependenciesNode.appendNode("dependency")
                        dependencyNode.appendNode("groupId", dependency.group)
                        dependencyNode.appendNode("artifactId", dependency.name)
                        dependencyNode.appendNode("version", dependency.version)
                        dependencyNode.appendNode("scope", scopeMapping[key])
                    }
                } catch (thr: Throwable) {

                }
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

    private fun addSourceJar(project: Project): Task {
        val sourceSet = mutableSetOf<File>()
        if (Util.isAndroidModule(project)) {
            val appExtension = project.extensions.getByType(LibraryExtension::class.java)
            appExtension.sourceSets.filter {
                it.name == "main"
            }.forEach {
                it.java.include("**/*.kt")
                sourceSet.addAll(it.java.srcDirs)
            }
        } else {
            val srcDirs = project.extensions.getByType(JavaPluginConvention::class.java)
                    .sourceSets.getByName("main").java.srcDirs
            sourceSet.addAll(srcDirs)
        }
        return project.tasks.create("sourceJar", Jar::class.java).apply {
            classifier = "sources"
            version = ""
            from(sourceSet)
        }
    }
}