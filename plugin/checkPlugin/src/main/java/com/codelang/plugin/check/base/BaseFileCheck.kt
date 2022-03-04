package com.codelang.plugin.check.base

import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/23.
 */
interface BaseFileCheck {
    fun onStart()

    fun onIteratorFile(path:String,dependency:String,fileName: String, fileSize: Long, zipInputStream: ZipInputStream)

    fun onEnd()
}