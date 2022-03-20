package com.codelang.upload.utils

import java.io.File

/**
 * @author wangqi
 * @since 2022/3/19.
 */
object FileUtils {
     fun copyFolder(src: File, dest: File) {
        if (src.isDirectory) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            src.list()?.forEach {
                val srcFile = File(src, it)
                val destFile = File(dest, it)
                // 递归复制
                copyFolder(srcFile, destFile);
            }
        } else {
            src.runCatching {
                inputStream().use { input ->
                    dest.apply {
                        outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }.onFailure {
                println("error: " + it.message)
            }
        }
    }


}