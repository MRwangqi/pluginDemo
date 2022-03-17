package com.codelang.upload.utils

import com.android.tools.build.apkzlib.sign.SigningExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension

object Util {

    fun isApplication(project: Project): Boolean {
        return project.plugins.hasPlugin("com.android.application")
    }

    fun isAndroidModule(project: Project): Boolean {
        return project.hasProperty("android")
    }

    //publishing
    fun publishingExtension(project: Project): PublishingExtension? {
        return project.extensions.findByType(PublishingExtension::class.java)
    }

}
