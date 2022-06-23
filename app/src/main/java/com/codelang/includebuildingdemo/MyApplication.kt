package com.codelang.includebuildingdemo

import android.app.Application
import com.codelang.includebuildingdemo.hook.ClassLoaderUtils

open class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ClassLoaderUtils.patch(this)
    }

}
