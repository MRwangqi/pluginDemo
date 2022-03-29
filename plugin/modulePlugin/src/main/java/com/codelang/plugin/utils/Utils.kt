package com.codelang.plugin.utils

import org.gradle.api.Project

object Utils {

    private const val GAV_ENABLE = "gav2project.enable"
    private const val GAV_SKIPS = "gav2project.skips"

    fun isEnable(project: Project): Boolean {
        try {
            if (project.rootProject.hasProperty(GAV_ENABLE)) {
                return project.rootProject.properties[GAV_ENABLE].toString().toBoolean()
            }
        } catch (e: Exception) {
        }
        return true
    }

    fun getSkips(project: Project): List<String> {
        try {
            if (project.rootProject.hasProperty(GAV_SKIPS)) {
                return project.rootProject.properties[GAV_SKIPS].toString().split(",")
            }
        } catch (e: Exception) {
        }
        return arrayListOf()
    }
}