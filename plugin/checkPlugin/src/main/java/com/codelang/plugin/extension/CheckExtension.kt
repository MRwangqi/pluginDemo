package com.codelang.plugin.extension

/**
 * @author wangqi
 * @since 2022/2/27.
 */
open class CheckExtension {
    var buildVariants: String = ""
    var manifestWhiteFile: String = ""

    override fun toString(): String {
        return "CheckExtension(buildVariants='$buildVariants',  manifestWhiteFile='$manifestWhiteFile')"
    }


}