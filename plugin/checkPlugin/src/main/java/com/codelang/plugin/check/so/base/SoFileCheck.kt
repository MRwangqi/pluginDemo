package com.codelang.plugin.check.so.base

import com.codelang.plugin.check.base.BaseFileCheck
import com.codelang.plugin.check.so.SoFileSize
import com.codelang.plugin.check.so.Support64bit
import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/23.
 */
class SoFileCheck : BaseFileCheck {

    private val list = arrayListOf<ISoFile>().apply {
        add(SoFileSize())
        add(Support64bit())
    }

    override fun onStart() {

    }

    override fun onIteratorFile(path: String, dependency: String, fileName: String, fileSize: Long, zipInputStream: ZipInputStream) {
        if (!fileName.endsWith(".so")) return
        list.forEach {
            it.onIteratorFile(path, dependency, fileName, fileSize, zipInputStream)
        }
    }

    override fun onEnd() {
        list.forEach {
            it.onEnd()
        }
    }
}

