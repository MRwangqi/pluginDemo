package com.codelang.upload.config

open class UploadConfig {
    var version :String= ""
    var artifactId :String= ""
    var groupId :String = ""
    var url:String = ""
    var hasPomDepend:Boolean = true
    override fun toString(): String {
        return "UploadConfig(version='$version', artifactId='$artifactId', groupId='$groupId', hasPomDepend=$hasPomDepend)"
    }
}