package com.codelang.plugin.check.so.base

import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/27.
 */
interface ISoFile {
    fun onIteratorFile(path:String,dependency:String,fileName: String, fileSize: Long, zipInputStream: ZipInputStream)

    fun onEnd()
}