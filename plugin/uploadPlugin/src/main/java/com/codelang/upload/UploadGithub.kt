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


class UploadGithub : BaseUploadPlugin() {


    override fun isSupportUpload(uploadConfig: UploadConfig,project: Project): Boolean {
        return true
    }

    override fun isCredentials(uploadConfig: UploadConfig): Boolean {
        return false
    }

    override fun uploadComplete(uploadConfig: UploadConfig, mavenUrl: String, project: Project) {
        val aarFile = project.file(mavenUrl)
        uploadGithub(aarFile, uploadConfig, project)
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

}