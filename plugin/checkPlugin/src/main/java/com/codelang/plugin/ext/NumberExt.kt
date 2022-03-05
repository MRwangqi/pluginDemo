package com.codelang.plugin.ext

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author wangqi
 * @since 2022/2/27.
 */


fun Long.toFileSize(): String {
    val sb = StringBuffer()
    val format = DecimalFormat("###.0")

    when {
        this >= 1024 * 1024 * 1024 -> {
            val i = (this / (1024.0 * 1024.0 * 1024.0))
            sb.append(format.format(i)).append("GB")
        }
        this >= 1024 * 1024 -> {
            val i = (this / (1024.0 * 1024.0))
            sb.append(format.format(i)).append("MB")
        }
        this >= 1024 -> {
            val i = (this / (1024.0))
            sb.append(format.format(i)).append("KB")
        }
        else -> {
            if (this <= 0) {
                sb.append("0B");
            } else {
                sb.append(this).append("B")
            }
        }
    }
    return sb.toString()
}


fun Long.toTime():String{
    val sdf2: SimpleDateFormat = SimpleDateFormat("yyyy 年 MM 月 dd 日 HH 时 mm 分 ss 秒")
    return sdf2.format(Date(this))
}

