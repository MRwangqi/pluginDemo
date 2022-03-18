package com.codelang.upload

import com.android.build.gradle.LibraryExtension
import com.codelang.upload.config.UploadConfig
import com.codelang.upload.utils.Util
import groovy.util.Node
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import java.io.File
import java.io.InputStreamReader

class UploadGithub : Plugin<Project> {

    /**
     * 复制一个目录及其子目录、文件到另外一个目录
     * @param src
     * @param dest
     * @throws IOException
     */
    private fun copyFolder(src: File, dest: File) {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            src.list()?.forEach {
                val srcFile = File(src, it)
                val destFile = File(dest, it)
                // 递归复制
                copyFolder(srcFile, destFile);
            }
        } else {
            src.runCatching {
                inputStream().use { input ->
                    dest.apply {
                        outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }.onFailure {
                println("error: " + it.message)
            }
        }
    }


    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin("maven-publish")) {
            project.plugins.apply("maven-publish")
        }
        project.extensions.create("upload", UploadConfig::class.java)

        project.afterEvaluate {
            val uploadConfig = project.extensions.findByName("upload") as UploadConfig

            println(uploadConfig)

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

                    publication.artifact(addSourceJar(project))

                    //pom config
                    publication.pom { pom ->
                        addDeveloper(pom)

                        // todo AGP 7.0 的实践效果看，默认就会把 dependencies 下的依赖打入了 pom
                        // todo 所以，这个地方改成，如果 hasPom 为 false 的话，则移除 dependencies
//                        applyPomDeps(pom = pom, project = project)
                        if (!uploadConfig.hasPomDepend) {
                            removePomDeps(pom)
                        }
                    }
                }
            }

            var url = uploadConfig.url
            if (url.isEmpty()) {
                url = "build/repo"
            }

            publishingExtension?.repositories {
                it.maven { repo ->
                    repo.url = project.file(url).toURI()
                }
            }

            project.task("upload") {
                it.dependsOn("publishMavenPublicationToMavenRepository")
            }

            project.tasks.filter {
                it.name == "publishMavenPublicationToMavenRepository"
            }.firstOrNull()?.doLast {
                val aarFile = project.file(url)
                println("\naar 路径: ${aarFile}")
                // todo 开始上传 github
                uploadGithub(aarFile, uploadConfig, project)
            }
        }

    }


    private fun uploadGithub(aarFile: File, uploadConfig: UploadConfig, project: Project) {

        if (uploadConfig.githubURL.isEmpty()) {
            println("githubURL is Null")
            return
        }

        // 获取 repo 的名称
        val lastIndex = uploadConfig.githubURL.lastIndexOf("/")
        val lastIndex2 = uploadConfig.githubURL.lastIndexOf(".git")
        val repo = uploadConfig.githubURL.substring(lastIndex + 1, lastIndex2)

        // 将 aar copy 到 Maven 目录
        val repoFile = File(project.buildDir.absolutePath + File.separator + repo)

        println("aarFile: " + aarFile.absoluteFile)
        println("mavenFile: " + repoFile.absoluteFile)

        if (!repoFile.exists()) {
            project.exec {
                it.workingDir = project.buildDir
                if (uploadConfig.githubBranch.isEmpty()) {
                    it.commandLine("git", "clone", uploadConfig.githubURL)
                } else {
                    it.commandLine("git", "clone", "-b", uploadConfig.githubBranch, uploadConfig.githubURL)
                }
            }
        }
        // 将 aar 拷贝到 repo 目录
        copyFolder(aarFile, repoFile)

        val arrFilePath = uploadConfig.groupId.replace(".", File.separator) + File.separator + uploadConfig.artifactId
        // push 到 maven
        project.exec {
            it.workingDir = File(project.buildDir.absolutePath + File.separator + repo)

            println(" arrFilePath=${arrFilePath} ${it.workingDir.exists()}  ${it.workingDir}")

            it.commandLine("git", "add", arrFilePath)
            val dependency = "implementation '${uploadConfig.groupId}:${uploadConfig.artifactId}:${uploadConfig.version}"
            it.commandLine("git", "commit-m", "添加 ${dependency}依赖")
            if (uploadConfig.githubBranch.isEmpty()) {
                it.commandLine("git", "push")
            } else {
                it.commandLine("git", "push", "origin", uploadConfig.githubBranch)
            }

            // git@github.com:MRwangqi/Maven.git
            // 获取 repo 的名称
            val l = uploadConfig.githubURL.lastIndexOf(":")
            val l2 = uploadConfig.githubURL.lastIndexOf("/")
            val userName = uploadConfig.githubURL.substring(l + 1, l2)

            println("已上传到 github=${uploadConfig.githubURL} 仓库")
            println("""
                maven 镜像源为:
                
                maven{
                   url "https://raw.githubusercontent.com/${userName}/${repo}/${uploadConfig.githubBranch}"
                }
            """.trimIndent())
            println("可添加依赖使用:\nimplementation '${uploadConfig.groupId}:${uploadConfig.artifactId}:${uploadConfig.version}'\n")
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