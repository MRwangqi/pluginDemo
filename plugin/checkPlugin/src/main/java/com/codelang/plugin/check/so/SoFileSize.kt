package com.codelang.plugin.check.so

import com.codelang.plugin.check.so.base.ISoFile
import com.codelang.plugin.check.so.bean.SoFile
import com.codelang.plugin.ext.toFileSize
import com.codelang.plugin.html.Html
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
        println()
        var result = ""
        println("==================== so 大小检查 ============================")
        val soList = hashMap.flatMap { it.value }.toList()
        val soSize = soList.sumOf { it.fileSize }
        println("-------------------> 总共有 ${soList.size} 个 so 文件，占用大小：${soSize.toFileSize()}")
        // 按依赖 so 的总体大小进行降序排序输出
        hashMap.map { entry ->
            Pair(entry.key, entry.value.sumOf { it.fileSize })
        }.sortedByDescending { it.second }.forEach {
            println("so = ${it.first}")
            hashMap[it.first]?.forEach {
                println("---> fileName=" + it.fileName + " fileSize=" + it.fileSize.toFileSize())
            }

            result += generatorPre(hashMap[it.first]?.firstOrNull()?.filePath
                    ?: "", it.first, hashMap[it.first] ?: arrayListOf())
        }
        //todo
        Html.content = generatorHtml(result,soList.size.toString(),soSize.toFileSize())
    }

    private fun generatorPre(path: String, fileName: String, soFile: ArrayList<SoFile>): String {
        var s = """
             <span class="location"><a href="$path">$fileName</a></span>
             <pre class="errorlines">
        """.trimIndent()

        soFile.forEachIndexed { index: Int, soFile ->
            s += """
            <span class="lineno"> $index </span>    so 文件 <span class="string"> ${soFile.fileName} </span>文件大小<span class="string"> ${soFile.fileSize.toFileSize()} </span>
        """.trimIndent()
            s += "\n"
        }
        s += """
             </pre>
        """.trimIndent()
        return s
    }


    private fun generatorHtml(pres: String,soFiles:String,fileSize:String): String {
        return """
            <section class="section--center mdl-grid mdl-grid--no-spacing mdl-shadow--2dp"
                     id="GradleDependencyCard" style="display: block;">
                <div class="mdl-card mdl-cell mdl-cell--12-col">
                    <div class="mdl-card__title">
                        <h2 class="mdl-card__title-text">so 大小检查(总共有 $soFiles 个 so 文件，占用 ${fileSize})</h2>
                    </div>
                    <div class="mdl-card__supporting-text">
                        <div class="issue">
                            <div class="warningslist">
$pres
                         </div>
                    </div>
                  </div>
                </div>
            </section>            
        """.trimIndent()
    }
}