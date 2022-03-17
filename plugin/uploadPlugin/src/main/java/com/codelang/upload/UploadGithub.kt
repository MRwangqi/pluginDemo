package com.codelang.upload

import com.android.build.gradle.AppExtension
import com.codelang.upload.utils.Util
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.net.URI

class UploadGithub : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin("maven-publish")) {
            project.plugins.apply("maven-publish")
        }

        project.afterEvaluate {
            val publishingExtension = Util.publishingExtension(project)

            publishingExtension?.publications {
                it.register(
                        "maven",
                        MavenPublication::class.java
                ) { publication ->

                    publication.from(project.components.findByName("release"))

                    println("release ---- "+project.components.findByName("release"))
                    publication.groupId = "com.aa.bb"
                    publication.artifactId = "haha"
                    publication.version = "1.0.0"
//                publication.artifact(addSourceJar(project))

                    //pom config
//                publication.pom { pom ->
//                    pom.developers { develops ->
//                    }
//                    applyPomDeps(pom = pom, project = project)
//                }

                    println("publications ---- "+publication)
                }
                println("publishing ---- "+publishingExtension)
            }

            publishingExtension?.repositories {
                it.maven { repo ->
                    repo.url = URI.create("../build/repo")
                }
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


    private fun addSourceJar(project: Project): Task {
        val sourceSet = mutableSetOf<File>()
        if (Util.isAndroidModule(project)) {
            val appExtension = project.extensions.getByType(AppExtension::class.java)
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