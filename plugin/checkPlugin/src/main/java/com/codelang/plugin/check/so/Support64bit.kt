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
class Support64bit : ISoFile {
    private val hashMap = HashMap<String, ArrayList<SoFile>>()

    override fun onIteratorFile(path: String, dependency: String, fileName: String, fileSize: Long, zipInputStream: ZipInputStream) {
        var list = hashMap[dependency]
        if (list == null) {
            list = ArrayList()
            hashMap[dependency] = list
        }

        list.add(SoFile(path, fileName, fileSize))
    }

    override fun onEnd() {
        println()
        println("==================== so 64 位检查 ============================")
        // 32bit : x86、armeabi、armeabi-v7a
        // 64bit : arm64-v8a 、x86_64


        // 不支持 64 bit so 列表
        val map = hashMap.filter { entry ->
            val isSupport64 = entry.value.filter {
                it.fileName.contains("arm64-v8a")
                        || it.fileName.contains("x86_64")
            }.toList().isNotEmpty()
            !isSupport64
        }.toMap()

        // 生成 html dom 节点
        generatorHtmlDom(map)
    }


    private fun generatorHtmlDom(map: Map<String, ArrayList<SoFile>>) {
        var result = ""
        map.forEach { entry ->
            result += generatorPre(entry.value[0].filePath, entry.key, entry.value)
        }
        //todo 添加到 html
        Html.selector.add(generatorSection(result))
    }


    private fun generatorPre(path: String, fileName: String, list: ArrayList<SoFile>): String {
        var s = """
            <span class="location"><a href="$path">$fileName</a></span>:
            <table class="overview">
        """.trimIndent()

        list.forEachIndexed { index: Int, soFile ->
            s += """
            <tr>
                <td class="issueColumn">
                    <i class="material-icons warning-icon">warning</i>
                    <span>${soFile.fileName}</span>
                </td>
            </tr>
        """.trimIndent()
            s += "\n"
        }
        s += """
             </table>
        """.trimIndent()
        return s
    }


    private fun generatorSection(pres: String): String {
        return """
            <section class="section--center mdl-grid mdl-grid--no-spacing mdl-shadow--2dp"
                     id="GradleDependencyCard" style="display: block;">
                <div class="mdl-card mdl-cell mdl-cell--12-col">
                    <div class="mdl-card__title">
                        <h2 class="mdl-card__title-text">64 bit so 检查(以下依赖未适配 64 位 so)</h2>
                    </div>
                    <div class="mdl-card__supporting-text">
$pres
                  </div>
                </div>
            </section>            
        """.trimIndent()
    }
}