package com.codelang.includebuildingdemo.hook;


import android.util.Log;

import dalvik.system.PathClassLoader;

public class ProxyClassLoader extends PathClassLoader {

    private static final String TAG = "ProxyClassLoader";

    private final ClassLoader mOrig;

    public ProxyClassLoader(ClassLoader parent, ClassLoader orig) {
        super("", "", parent);
        mOrig = orig;
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        Class<?> c = null;
        try {
            Log.e(TAG, "loadClass=" + className);
            // 需要用原始的 classloader 去 loadClass，不然无法加载 DexPathList 中的 class
            c = mOrig.loadClass(className);
            return c;
        } catch (Throwable e) {
        }
        return super.loadClass(className, resolve);
    }
}
