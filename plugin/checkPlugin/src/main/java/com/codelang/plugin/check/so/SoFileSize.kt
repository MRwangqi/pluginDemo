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
            list.add(SoFile(path, fileName, bytes.size.toLong()))
        } else {
            list.add(SoFile(path, fileName, fileSize))
        }
    }

    override fun onEnd() {
        var template = ""
        println()
        println("==================== so 大小检查 ============================")
        val soList = hashMap.flatMap { it.value }.toList()
        val soSize = soList.sumOf { it.fileSize }
        println("-------------------> 总共有 ${soList.size} 个 so 文件，占用大小：${soSize.toFileSize()}")
        // 按依赖 so 的总体大小进行降序排序输出
        hashMap.map { entry ->
            Pair(entry.key, entry.value.sumOf { it.fileSize })
        }.sortedByDescending { it.second }.forEach {
            println("so = ${it.first}")

            var soContent = ""
            hashMap[it.first]?.forEachIndexed { index, soFile ->
                val text = "fileName=" + soFile.fileName + " fileSize=" + soFile.fileSize.toFileSize()
                soContent += """
                    <span class="lineno"> $index </span>    so <span class="string">$text</span>
                """.trimIndent() + "\n"

                println(text)
            }
            soContent = ""

            template += """
                 <span class="location"><a href="${hashMap[it.first]?.get(0)?.filePath}">${it.first}</a></span>
                 <pre class="errorlines">
                  $soContent
                 </pre>
           """.trimIndent() + "\n"
        }

         """
             <div class="warningslist">
             $template
             </div>
        """.trimIndent()
    }

}