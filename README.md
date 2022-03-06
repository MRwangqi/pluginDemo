### 一、依赖配置

配置 maven 镜像源和依赖
```java
buildscript {
    repositories {
        ...
        // 配上本地 maven 源
        maven{
           url "https://raw.githubusercontent.com/MRwangqi/Maven/main"
        }
    }
    dependencies {
        classpath "com.android.tools.build:gradle:7.0.4"
        // 依赖 check 插件
        classpath "com.github.MRwangqi:checkPlugin:1.0.0"
    }
}
```

然后在 app 工程的 build.gradle 中依赖插件，并且在工程下面配置白名单文件：

```java
plugins {
    id 'com.android.application'
    // apply check 插件
    id 'checkPlugin'
}

check{
    // 配置白名单
    manifestWhiteFile="ManifestWhite.xml"
}

ManifestWhite.xml 文件如下：

```java
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.codelang.includebuildingdemo">

    <!--  插件会读取 uses-sdk ，如果分析出的依赖不等于 targetSdk 或是如果不等 minSDK 则会输出分析-->
    <uses-sdk android:minSdkVersion="14" android:targetSdkVersion="30" />

    <!--  插件会读取 uses-permission ，如果分析出的依赖权限不在下面则会输出分析-->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>

</manifest>
```


### 二、执行检查命令：

执行命令模板如下：
> ./gradlew checkDependency -Pbuild=${build variant}

要执行的 build variant 可以在 Android studio 中查看：
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5a6f62c79ade4ae7864c572e852ee9b8~tplv-k3u1fbpfcp-zoom-1.image)
比如我们要检查 debug 的依赖分析，则命令如下：
> ./gradlew checkDependency -Pbuild=debug

当然，也可以直接使用如下命令进行检查，插件默认的 build variant 是 debug
> ./gradlew checkDependency


结果会在 build 目录的 checkPlugin 生成一份 check.html 文件，你可以直接查看 demo 生成的结果报告：[https://mrwangqi.github.io/pluginDemo/](https://mrwangqi.github.io/pluginDemo/)
