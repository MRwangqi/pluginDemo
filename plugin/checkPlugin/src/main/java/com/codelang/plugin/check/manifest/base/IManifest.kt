package com.codelang.plugin.check.manifest.base

import groovy.util.Node
import java.util.zip.ZipInputStream

/**
 * @author wangqi
 * @since 2022/2/27.
 */
interface IManifest {
    fun onNode(parentNode: Node,path:String,dependency: String, fileName: String, fileSize: Long, zipInputStream: ZipInputStream)
    fun onEnd()
}