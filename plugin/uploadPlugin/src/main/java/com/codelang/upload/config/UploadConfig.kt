package com.codelang.upload.config

open class UploadConfig {
    var version: String = ""
    var artifactId: String = ""
    var groupId: String = ""
    var sourceJar: Boolean = true
    var hasPomDepend: Boolean = true


    // github 相关
    var githubURL: String = ""
    var githubBranch: String = ""

    // nexus 相关
    var nexusURL: String = ""
    var nexusName: String = ""
    var nexusPsw: String = ""


    override fun toString(): String {
        return "UploadConfig(version='$version', artifactId='$artifactId', groupId='$groupId', sourceJar=$sourceJar, githubURL='$githubURL', githubBranch='$githubBranch', hasPomDepend=$hasPomDepend, nexusURL='$nexusURL', nexusName='$nexusName', nexusPsw='$nexusPsw')"
    }


}