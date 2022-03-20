package com.codelang.upload


import com.codelang.upload.config.UploadConfig
import org.gradle.api.Project
import java.net.URI

class UploadMaven : BaseUploadPlugin() {

    private val PARAM_NAME = "name"
    private val PARAM_PSW = "psw"

    override fun isSupportUpload(uploadConfig: UploadConfig, project: Project): Boolean {
        // ./gradlew upload -Pname=admin -Ppsw=admin
        val params = project.gradle.startParameter.projectProperties

        if (params.containsKey(PARAM_NAME)) {
            uploadConfig.nexusName = params[PARAM_NAME] ?: ""
        }
        if (params.containsKey(PARAM_PSW)) {
            uploadConfig.nexusPsw = params[PARAM_PSW] ?: ""
        }


        if (uploadConfig.nexusURL.isEmpty() ||
                uploadConfig.nexusName.isEmpty() ||
                uploadConfig.nexusPsw.isEmpty()) {

            println("upload 配置的 nexus 有空值:")
            println("nexusURL=${uploadConfig.nexusURL}")
            println("nexusName=${uploadConfig.nexusName}")
            println("nexusPsw=${uploadConfig.nexusPsw}")

            println("upload 采取降级发布到本地目录}")
        }
        return true
    }

    override fun isCredentials(uploadConfig: UploadConfig): Boolean {
        if (uploadConfig.nexusURL.startsWith("http")
                || uploadConfig.nexusURL.startsWith("https")) {
            return true
        }
        return false
    }

    override fun uploadComplete(uploadConfig: UploadConfig, mavenUrl: String, project: Project) {
        println("""
                已上传到仓库, maven 镜像源为:

                maven{
                   url "$mavenUrl"
                }

                可添加依赖使用:
                
                implementation '${uploadConfig.groupId}:${uploadConfig.artifactId}:${uploadConfig.version}'
            """.trimIndent())
    }
}