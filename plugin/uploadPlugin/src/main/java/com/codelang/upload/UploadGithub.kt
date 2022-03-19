package com.codelang.upload

import com.codelang.upload.config.UploadConfig
import com.codelang.upload.task.androidSourcesJar
import com.codelang.upload.task.emptySourcesJar
import com.codelang.upload.task.javaSourcesJar
import com.codelang.upload.utils.FileUtils
import com.codelang.upload.utils.Util
import groovy.util.Node
import org.eclipse.jgit.api.Git
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import java.io.File
import java.io.InputStreamReader


class UploadGithub : Plugin<Project> {


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

                        // todo AGP 7.0 的实践效果看，默认就会把 dependencies 下的依赖打入了 pom
                        // todo 所以，这个地方改成，如果 hasPom 为 false 的话，则移除 dependencies
//                        applyPomDeps(pom = pom, project = project)
                        if (!uploadConfig.hasPomDepend) {
                            removePomDeps(pom)
                        }
                    }
                }
            }

            val mavenUrl = "../build/repo"

            publishingExtension?.repositories {
                it.maven { repo ->
                    repo.url = project.file(mavenUrl).toURI()
                }
            }

            project.task("upload") {
                it.dependsOn("publishMavenPublicationToMavenRepository")
            }

            project.tasks.filter {
                it.name == "publishMavenPublicationToMavenRepository"
            }.firstOrNull()?.doLast {
                val aarFile = project.file(mavenUrl)
                //  开始上传 github
                uploadGithub(aarFile, uploadConfig, project)
            }
        }

    }


    private fun uploadGithub(aarFile: File, uploadConfig: UploadConfig, project: Project) {
        if (uploadConfig.githubURL.isEmpty()) {
            println("githubURL is Null,aar 已发布到本地路径:${aarFile.absolutePath}")
            return
        }

        // 获取 repo 的名称
        val lastIndex = uploadConfig.githubURL.lastIndexOf("/")
        val lastIndex2 = uploadConfig.githubURL.lastIndexOf(".git")
        val repoName = uploadConfig.githubURL.substring(lastIndex + 1, lastIndex2)


        val buildDir = project.file("../build")
        // 将 aar copy 到 Maven 目录
        val gitWorkDir = project.file("../build/$repoName")

        println("aarFile: " + aarFile.absoluteFile)
        println("mavenFile: " + gitWorkDir.absoluteFile)


        if (!gitWorkDir.exists()) {
            project.exec {
                it.workingDir = buildDir
                if (uploadConfig.githubBranch.isEmpty()) {
                    it.commandLine("git", "clone", uploadConfig.githubURL)
                } else {
                    it.commandLine("git", "clone", "-b", uploadConfig.githubBranch, uploadConfig.githubURL)
                }
            }
        }

        // 将 aar 拷贝到 repo 目录
        FileUtils.copyFolder(aarFile, gitWorkDir)


        val git = Git.open(gitWorkDir)

        val arrName = uploadConfig.groupId.replace(".", File.separator) + File.separator + uploadConfig.artifactId

        // git diff 比较，如果没有可提交的内容，则直接 return 不处理
        val diffArray = git.diff().setDestinationPrefix(arrName).call()
        println("diff=${diffArray}")
        if (diffArray.size == 0) {
            println("git diff $arrName 时发现 aar 文件没有可提交的内容")
            return
        }

        // git add
        git.add().addFilepattern(arrName).call()
        // git commit
        val dependency = "${uploadConfig.groupId}:${uploadConfig.artifactId}:${uploadConfig.version}"
        git.commit().setMessage(dependency).call()


        // push 到 maven
        project.exec {
            it.workingDir = gitWorkDir
            if (uploadConfig.githubBranch.isEmpty()) {
                it.commandLine("git", "push")
            } else {
                println("git push origin ${uploadConfig.githubBranch}")
                it.commandLine("git", "push", "origin", uploadConfig.githubBranch)
            }
        }


        // 获取 repo 的名称
        val l = uploadConfig.githubURL.lastIndexOf(":")
        val l2 = uploadConfig.githubURL.lastIndexOf("/")
        val userName = uploadConfig.githubURL.substring(l + 1, l2)

        println("""
                已上传到 github=${uploadConfig.githubURL} 仓库, maven 镜像源为:
                
                maven{
                   url "https://raw.githubusercontent.com/${userName}/${repoName}/${uploadConfig.githubBranch}"
                }
                
                可添加依赖使用:
                implementation '${uploadConfig.groupId}:${uploadConfig.artifactId}:${uploadConfig.version}'
            """.trimIndent())

    }


    private fun addPomDeps(pom: MavenPom, project: Project) {
        val scopeMapping = mapOf<String, String?>(
                "api" to "compile",
                "implementation" to "compile",
                "compile" to "compile"
        )
        pom.withXml { xml ->
            val dependenciesNode = xml.asNode().appendNode("dependencies")
            scopeMapping.keys.forEach { key ->
                try {
                    project.configurations.getByName(key).allDependencies.forEach { dependency ->
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