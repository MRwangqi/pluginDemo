package com.codelang.plugin.check.manifest

object ManifestConfig {
    var usesSdk: UsesSdk = UsesSdk(-1,-1)
    var permissions = arrayListOf<String>()
}

data class UsesSdk(val minSdkVersion: Int, val targetSdkVersion: Int)