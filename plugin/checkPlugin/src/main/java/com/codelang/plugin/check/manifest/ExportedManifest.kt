package com.codelang.plugin.check.manifest

import com.codelang.plugin.check.manifest.base.IManifest
import com.codelang.plugin.check.manifest.bean.ExportedFile
import com.codelang.plugin.html.IndexHtml
import groovy.util.Node
import java.util.zip.ZipInputStream

class ExportedManifest : IManifest {

    private val hashMap = HashMap<String, ArrayList<ExportedFile>>()


    override fun onNode(parentNode: Node, path: String, dependency: String, fileName: String, fileSize: Long, zipInputStream: ZipInputStream) {
        val application = parentNode.children().find {
            (it as? Node)?.name()?.equals("application") ?: false
        } as Node?

        if (application != null) {
            val list = ArrayList<Node>()

            application.children().forEach {
                val node = (it as Node)

                if (node.name().equals("activity")) {
                    list.add(node)
                    node.children().forEach { alias ->
                        val aliasNode = (alias as Node)
                        if (aliasNode.name().equals("activity-alias")) {
                            list.add(aliasNode)
                        }
                    }
                }

                if (node.name().equals("service") || node.name().equals("receiver")) {
                    list.add(node)
                }
            }

            list.forEach { node ->
                val intentFilter = node.children().find {
                    (it as Node).name().equals("intent-filter")
                } as Node?

                if (intentFilter != null && node.attribute("android:exported") == null) {
                    //todo 需要显示申明 exported
                    var deps = hashMap[dependency]
                    if (deps == null) {
                        deps = ArrayList()
                        hashMap[dependency] = deps
                    }

                    deps.add(ExportedFile(path, node.name().toString(), node.attribute("android:name")?.toString()
                            ?: ""))

                }
            }
        }
    }

    override fun onEnd() {
        if (hashMap.isNotEmpty()) {
            generatorHtmlDom(hashMap)
        }
    }

    private fun generatorHtmlDom(map: HashMap<String, ArrayList<ExportedFile>>) {
        var result = ""
        map.forEach { entry ->
            result += generatorPre(entry.value[0].path, entry.key, entry.value)
        }
        IndexHtml.insertSection(generatorSection(result))
    }

    private fun generatorPre(path: String, fileName: String, list: ArrayList<ExportedFile>): String {
        var s = """
             <span class="location"><a href="$path">$fileName</a></span>
             <pre class="errorlines">
        """.trimIndent()

        list.forEachIndexed { index: Int, exportedFile ->
            s += """
            <span class="lineno"> $index </span><span class="string"> &lt;${exportedFile.nodeName}&gt;${exportedFile.className}&lt;/&gt; </span>
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
                        <h2 class="mdl-card__title-text">exported 检查(以下依赖未显示设置 exported)</h2>
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
