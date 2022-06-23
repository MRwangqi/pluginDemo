
package com.codelang.includebuildingdemo.hook;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

/**
 * 参考于 com.qihoo360.loader.utils.PatchClassLoaderUtils
 */
public class ClassLoaderUtils {

    private static final String TAG = "ClassLoaderUtils";

    public static boolean patch(@NonNull Application application) {
        try {
            Context oBase = application.getBaseContext();

            // 获取 mBase.mPackageInfo
            Object oPackageInfo = ReflectUtils.readField(oBase, "mPackageInfo");

            // 获取 mPackageInfo.mClassLoader
            ClassLoader oClassLoader = (ClassLoader) ReflectUtils.readField(oPackageInfo, "mClassLoader");

            // 创建新的 classloader
            ClassLoader cl = new ProxyClassLoader(oClassLoader.getParent(), oClassLoader);

            // 将新的 ClassLoader 写入 mPackageInfo.mClassLoader
            ReflectUtils.writeField(oPackageInfo, "mClassLoader", cl);

            // 设置线程上下文中的 ClassLoader 为 cl
            // 防止在个别 Java 库用到了 Thread.currentThread().getContextClassLoader() 时，“用了原来的PathClassLoader”，或为空指针
            Thread.currentThread().setContextClassLoader(cl);

        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
