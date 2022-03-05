package com.codelang.plugin.check.manifest

import com.codelang.plugin.check.manifest.base.IManifest
import com.codelang.plugin.check.manifest.bean.ManifestConfig
import com.codelang.plugin.html.IndexHtml
import groovy.util.Node
import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/23.
 */
class UsesSDKManifest : IManifest {

    private val hashMap = HashMap<String, Pair<String,ArrayList<String>>>()

    override fun onNode(parentNode: Node, path: String, dependency: String, fileName: String, fileSize: Long, zipInputStream: ZipInputStream) {
        //todo uses-sdk 解析,解析这个的目的：
        //todo 1、依赖库可能适配了 android12(targetSDK 为 31)，使用了 android12 相关的特性， 但项目的 targetSDK 却为 27，导致不可预期的适配问题
        val useSdkNode = parentNode.children().find {
            (it as? Node)?.name()?.equals("uses-sdk") ?: false
        } as Node?

        if (useSdkNode != null) {
            val min = useSdkNode.attribute("android:minSdkVersion")
            val target = useSdkNode.attribute("android:targetSdkVersion")

            val list = arrayListOf<String>()
            hashMap[dependency] = Pair(path,list)
            if (min != null && ManifestConfig.usesSdk.minSdkVersion != -1) {
                val minInt = min.toString().toInt()
                if (minInt != ManifestConfig.usesSdk.minSdkVersion) {
                    // 需要记录
                    list.add("android:minSdkVersion=$minInt")
                }
            }
            if (target != null && ManifestConfig.usesSdk.targetSdkVersion != -1) {
                val targetInt = target.toString().toInt()
                if (targetInt != ManifestConfig.usesSdk.targetSdkVersion) {
                    // 需要记录
                    list.add("android:targetSdkVersion=$targetInt")
                }
            }
        }
    }

    override fun onEnd() {
       val map =  hashMap.filter { it.value.second.isNotEmpty() }.toMap()

        if (map.isNotEmpty()){
            generatorHtmlDom(map)
        }

    }



    private fun generatorHtmlDom(map: Map<String, Pair<String,ArrayList<String>>>) {
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
            <span class="lineno"> $index </span><span class="string">$node </span>
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
                        <h2 class="mdl-card__title-text">uses-sdk 检查(应用 minSDK=${ManifestConfig.usesSdk.minSdkVersion} targetSDK=${ManifestConfig.usesSdk.targetSdkVersion})</h2>
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