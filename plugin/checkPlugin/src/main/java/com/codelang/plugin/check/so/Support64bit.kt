package com.codelang.plugin.check.so

import com.codelang.plugin.check.so.base.ISoFile
import com.codelang.plugin.check.so.bean.SoFile
import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/27.
 */
class Support64bit : ISoFile {
    private val hashMap = HashMap<String, ArrayList<SoFile>>()

    override fun onIteratorFile(path: String, dependency: String, fileName: String, fileSize: Long, zipInputStream: ZipInputStream) {
        var list = hashMap[dependency]
        if (list == null) {
            list = ArrayList()
            hashMap[dependency] = list
        }

        list.add(SoFile(path,fileName, fileSize))
    }

    override fun onEnd() {
        println()
        println("==================== so 64 位检查 ============================")
        // 32bit : x86、armeabi、armeabi-v7a
        // 64bit : arm64-v8a 、x86_64
        hashMap.forEach { (t, u) ->
            val isSupport64 = u.filter {
                it.fileName.contains("arm64-v8a")
                        || it.fileName.contains("x86_64")
            }.toList().isNotEmpty()

            if (!isSupport64) {
                println("so = $t 需要适配 64 位 so :")
                println("---> so is =${u.map { it.fileName }.toList()}")
            }

        }
    }
}