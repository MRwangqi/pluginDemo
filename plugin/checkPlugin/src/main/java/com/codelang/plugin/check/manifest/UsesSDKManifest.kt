package com.codelang.plugin.check.manifest

import com.codelang.plugin.check.manifest.base.IManifest
import com.codelang.plugin.check.manifest.bean.ManifestConfig
import groovy.util.Node
import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/23.
 */
class UsesSDKManifest : IManifest {

    private val hashMap = HashMap<String, ArrayList<String>>()

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
            hashMap[dependency] = list
            if (min != null && ManifestConfig.usesSdk.minSdkVersion != -1) {
                val minInt = min.toString().toInt()
                if (minInt < ManifestConfig.usesSdk.minSdkVersion) {
                    // 需要记录
                    list.add("android:minSdkVersion=$minInt")
                }
            }
            if (target != null && ManifestConfig.usesSdk.targetSdkVersion != -1) {
                val targetInt = target.toString().toInt()
                if (targetInt > ManifestConfig.usesSdk.targetSdkVersion) {
                    // 需要记录
                    list.add("android:targetSdkVersion=$targetInt")
                }
            }
        }
    }

    override fun onEnd() {
        println()
        println("==================== uses-sdk 检查 ============================")
        hashMap.filter { it.value.isNotEmpty() }.forEach { (t, u) ->
            println("依赖 = $t 没有适配目标 uses-sdk : --> $u")
        }
    }
}