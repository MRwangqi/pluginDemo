package com.codelang.plugin.check.manifest

import com.codelang.plugin.check.manifest.base.IManifest
import com.codelang.plugin.check.manifest.bean.ManifestConfig
import com.codelang.plugin.html.IndexHtml
import groovy.util.Node
import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/28.
 */
class PermissionManifest : IManifest {
    private val hashMap = HashMap<String, Pair<String, ArrayList<String>>>()

    override fun onNode(parentNode: Node, path: String, dependency: String, fileName: String, fileSize: Long, zipInputStream: ZipInputStream) {
        parentNode.children()?.forEach {
            val node = (it as? Node)
            if (node != null && node.name().equals("uses-permission")) {

                val permission = node.attribute("android:name")?.toString() ?: return

                // 白名单没有配置的需要记录输出
                if (!ManifestConfig.permissions.contains(permission)) {
                    var pair = hashMap[dependency]
                    if (pair == null) {
                        pair = Pair(path, ArrayList())
                        hashMap[dependency] = pair
                    }
                    pair.second.add(permission)
                }
            }
        }
    }

    override fun onEnd() {
        if (hashMap.isNotEmpty()) {
            generatorHtmlDom(hashMap)
        }
    }


    private fun generatorHtmlDom(map: HashMap<String, Pair<String, ArrayList<String>>>) {
        var result = ""
        map.forEach { entry ->
            result += generatorPre(entry.value.first, entry.key, entry.value.second)
        }
        //todo 添加到 html
        IndexHtml.insertSection(generatorSection(result))
    }

    private fun generatorPre(path: String, fileName: String, list: ArrayList<String>): String {
        var s = """
             <span class="location"><a href="$path">$fileName</a></span>
             <pre class="errorlines">
        """.trimIndent()

        list.forEachIndexed { index: Int, node ->
            s += """
            <span class="lineno"> $index </span><span class="string"> &lt;uses-permission android:name="$node"&gt; </span>
        """.trimIndent()
            s += "\n"
        }
        s += """
             </pre>
        """.trimIndent()
        return s
    }


    private fun generatorSection(pres: String): String {
        return """
            <section class="section--center mdl-grid mdl-grid--no-spacing mdl-shadow--2dp"
                     id="GradleDependencyCard" style="display: block;">
                <div class="mdl-card mdl-cell mdl-cell--12-col">
                    <div class="mdl-card__title">
                        <h2 class="mdl-card__title-text">未匹配的权限检查(以下权限未在白名单中声明)</h2>
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