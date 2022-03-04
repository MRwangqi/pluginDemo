package com.codelang.plugin.check.so

import com.codelang.plugin.check.so.base.ISoFile
import com.codelang.plugin.check.so.bean.SoFile
import com.codelang.plugin.ext.toFileSize
import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/27.
 */
class SoFileSize : ISoFile {

    private val hashMap = HashMap<String, ArrayList<SoFile>>()


    override fun onIteratorFile(path: String, dependency: String, fileName: String, fileSize: Long, zipInputStream: ZipInputStream) {
        var list = hashMap[dependency]
        if (list == null) {
            list = ArrayList()
            hashMap[dependency] = list
        }

        if (fileSize == -1L) {
            // jar 包无法从 zipInputStream.size 拿到文件大小，这里换个方式，直接读取一遍文件内容拿到大小
            val bytes = zipInputStream.readBytes()
            list.add(SoFile(fileName, bytes.size.toLong()))
        } else {
            list.add(SoFile(fileName, fileSize))
        }
    }

    override fun onEnd() {
        println()
        println("==================== so 大小检查 ============================")
        // 按依赖 so 的总体大小进行降序排序输出
        hashMap.map { entry ->
            Pair(entry.key, entry.value.sumOf { it.fileSize })
        }.sortedByDescending { it.second }.forEach {
            println("so = ${it.first}")
            hashMap[it.first]?.forEach {
                println("---> fileName=" + it.fileName + " fileSize=" + it.fileSize.toFileSize())
            }
        }
    }
}