package com.codelang.plugin

object Utils {

    fun getRemovePermission(name: String): String {
        return """
            uses-permission android:name="$name" tools:node="remove"
        """.trimIndent()
    }


    fun getPermission(name: String): String {
        return """
             uses-permission android:name="$name"
        """.trimIndent()
    }
}