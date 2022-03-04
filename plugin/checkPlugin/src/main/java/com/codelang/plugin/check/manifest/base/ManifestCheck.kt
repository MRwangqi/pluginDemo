package com.codelang.plugin.check.manifest.base

/**
 * @author wangqi
 * @since 2022/2/27.
 */
import com.codelang.plugin.check.base.BaseFileCheck
import com.codelang.plugin.check.manifest.*
import com.codelang.plugin.config.Config
import groovy.util.Node
import groovy.util.XmlParser
import org.xml.sax.InputSource
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

class ManifestCheck : BaseFileCheck {

    private val list = arrayListOf<IManifest>().apply {
        add(ExportedManifest())
        add(PermissionManifest())
        add(UsesSDKManifest())
    }

    override fun onStart() {
        try {
            val file = File(Config.manifestWhiteFile)
            if (file.exists() && Config.manifestWhiteFile.endsWith(".xml")) {
                val parentNode = XmlParser(false, false)
                        .parse(file)
                parseUsesSdk(parentNode)
                parsePermissions(parentNode)
            } else {
                println("配置文件不存在,或是配置文件错误:" + Config.manifestWhiteFile)
            }
        } catch (e: Exception) {
            println("未配置权限检查文件,需要进行配置才能检查")
        }
    }

    override fun onIteratorFile(path: String, dependency: String, fileName: String, fileSize: Long, zipInputStream: ZipInputStream) {
        if (!fileName.endsWith("AndroidManifest.xml")) return

        // xml 解析无法直接使用 zipInputStream，会报 close 异常
        val text = BufferedReader(InputStreamReader(zipInputStream)).readText()

        // 解析的文本必须按如下进行包装成 input，直接给 text 进行 xml 解析会报 MalformedURLException 异常
        val ins = InputSource(ByteArrayInputStream(text.toByteArray()))


        val parentNode = XmlParser(false, false)
                .parse(ins)

        list.forEach {
            it.onNode(parentNode, path, dependency, fileName, fileSize, zipInputStream)
        }

    }

    override fun onEnd() {
        list.forEach {
            it.onEnd()
        }
    }


    private fun parseUsesSdk(parentNode: Node) {
        val useSdkNode = parentNode.children().find {
            (it as? Node)?.name()?.equals("uses-sdk") ?: false
        } as Node?
        if (useSdkNode == null) {
            println("未读取到 uses-sdk 的配置")
        } else {
            val min = useSdkNode.attribute("android:minSdkVersion")?.toString()?.toInt() ?: -1
            val target = useSdkNode.attribute("android:targetSdkVersion")?.toString()?.toInt() ?: -1
            ManifestConfig.usesSdk = UsesSdk(min, target)
        }
    }

    private fun parsePermissions(parentNode: Node) {
        parentNode.children()?.forEach {
            val node = (it as? Node)
            if (node != null && node.name().equals("uses-permission")) {
                node.attribute("android:name")?.let {
                    ManifestConfig.permissions.add(it.toString())
                }
            }
        }
    }
}
