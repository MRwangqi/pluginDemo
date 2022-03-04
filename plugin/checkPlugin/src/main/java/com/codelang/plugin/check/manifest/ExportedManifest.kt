package com.codelang.plugin.check.manifest

import com.codelang.plugin.check.manifest.base.IManifest
import com.codelang.plugin.check.manifest.bean.ExportedFile
import groovy.util.Node
import java.util.zip.ZipInputStream

class ExportedManifest : IManifest {

    private val hashMap = HashMap<String, ArrayList<ExportedFile>>()


    override fun onNode(parentNode: Node,path:String,dependency: String, fileName: String, fileSize: Long, zipInputStream: ZipInputStream) {
        val application = parentNode.children().find {
            (it as? Node)?.name()?.equals("application")?:false
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
                    deps.add(ExportedFile(node.name().toString(), node.attribute("android:name")?.toString()
                            ?: ""))

                }
            }
        }
    }

    override fun onEnd() {
        println()
        println("==================== exported 检查 ============================")
        hashMap.forEach { t, u ->
            println("$t 模块需要显示声明 exported:")
            u.forEach {
                println("---> node="+ it.nodeName +" className="+it.className)
            }
        }
    }
}
