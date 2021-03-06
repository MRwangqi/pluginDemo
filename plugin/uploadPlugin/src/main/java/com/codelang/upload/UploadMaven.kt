package com.codelang.upload


import com.codelang.upload.config.UploadConfig
import org.gradle.api.Project
import java.net.URI
import java.util.*

class UploadMaven : BaseUploadPlugin() {

    private val PARAMS_URL = "url"
    private val PARAM_NAME = "name"
    private val PARAM_PSW = "psw"

    override fun isSupportUpload(uploadConfig: UploadConfig, project: Project): Boolean {
        val properties =  Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        val nexusUrl = properties.getProperty("nexusURL")?:""
        if (nexusUrl.isNotEmpty()){
            uploadConfig.nexusURL = nexusUrl
        }
        val nexusName = properties.getProperty("nexusName")?:""
        if (nexusUrl.isNotEmpty()){
            uploadConfig.nexusName = nexusName
        }

        val nexusPsw = properties.getProperty("nexusPsw")?:""
        if (nexusUrl.isNotEmpty()){
            uploadConfig.nexusPsw = nexusPsw
        }


        // ./gradlew upload -Pname=admin -Ppsw=admin -Purl=http://localhost:8081/repository/android/
        val params = project.gradle.startParameter.projectProperties

        if (params.containsKey(PARAM_NAME)) {
            uploadConfig.nexusName = params[PARAM_NAME] ?: ""
        }
        if (params.containsKey(PARAM_PSW)) {
            uploadConfig.nexusPsw = params[PARAM_PSW] ?: ""
        }
        if (params.containsKey(PARAMS_URL)) {
            uploadConfig.nexusURL = params[PARAMS_URL] ?: ""
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