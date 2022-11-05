Class 分析插件，扫描出配置文件中描述的字符常量、字段与方法在哪个依赖的类中有所调用

### 一、依赖配置

配置 maven 镜像源和依赖
```java
buildscript {
    repositories {
        ...
        // maven 源
        maven{
           url "https://raw.githubusercontent.com/MRwangqi/Maven/main"
        }
    }
    dependencies {
        classpath "com.android.tools.build:gradle:7.0.4"
        // 依赖插件
        classpath "com.github.MRwangqi:classAnalysis:1.0.0"
    }
}
```

然后在 app 工程的 build.gradle 中依赖插件，并配置描述文件，描述文件可查看 app 工程目录下的 android13.json

```java
plugins {
    id 'com.android.application'
    // apply 插件
    id classAnalysisPlugin
}

classAnalysis {
   configFile = project.projectDir.absolutePath + File.separator + "android13.json"
}
```

### 二、配置文件说明


```json
{
  "stringRef": [
    "android.permission.READ_EXTERNAL_STORAGE"
  ],
  "fieldRef": [
    {
      "className": "android/app/ActivityThread",
      "fieldName": "mCurDefaultDisplayDpi",
      "signature": "I"
    }
  ],
  "methodRef": [
    {
      "className": "android/app/NotificationManager",
      "method": "createNotificationChannel",
      "signature": "(Landroid/app/NotificationChannel;)V"
    }
  ]
}

```
- stringRef : 描述字符常量的调用的情况。例如：检查所有依赖中，有哪些模块在调用 android.permission.READ_EXTERNAL_STORAGE 权限
- fieldRef : 描述字段的调用情况。例如：检查所有依赖中，有哪些模块在调用 ActivityThread.mCurDefaultDisplayDpi 字段
- methodRef : 描述方法的调用情况。例如：检查所有依赖中，有哪些模块在调用 NotificationManager.createNotificationChannel(NotificationChannel) 方法


fieldName、method、signature 可以为空，可理解理解为这几个字段可用于模糊检查与精确检查

### 三、执行检查命令：

执行命令模板如下：
> ./gradlew classParse -Pbuild=${build variant}

要执行的 build variant 可以在 Android studio 中查看：
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5a6f62c79ade4ae7864c572e852ee9b8~tplv-k3u1fbpfcp-zoom-1.image)

比如我们要检查 debug 的依赖分析，则命令如下：
> ./gradlew classAnalysis -Pbuild=debug

当然，也可以直接使用如下命令进行检查，插件默认的 build variant 是 debug
> ./gradlew classAnalysis

执行命令成功后，会在主工程的 build 目录生成 classAnalysis.json 文件，控制台也会打印出该文件的绝对路径，如：
```text
...
配置文件生成!!! /Users/codelang/base_plugin/pluginDemo/app/build/classAnalysis.json
```


### 四、执行结果：

生成的 classAnalysis.json 文件如下:

```json
{
  "stringRef": [
    {
      "name": "android.permission.READ_EXTERNAL_STORAGE",
      "ref": [
        {
          "dependencies": "\u0027:android-lib2:debugApiElements\u0027",
          "className": "com/codelang/android_lib2/TestCase"
        }
      ]
    }
  ],
  "methodRef": [
    {
      "className": "android/app/NotificationManager",
      "method": "createNotificationChannel",
      "signature": "(Landroid/app/NotificationChannel;)V",
      "ref": [
        {
          "dependencies": "\u0027:android-lib2:debugApiElements\u0027",
          "className": "com/codelang/android_lib2/TestCase"
        },
        {
          "dependencies": "androidx.core:core:1.7.0",
          "className": "androidx/core/app/NotificationManagerCompat"
        }
      ]
    }
  ],
  "fieldRef": [
    {
      "className": "android/app/ActivityThread",
      "fieldName": "mCurDefaultDisplayDpi",
      "signature": "I",
      "ref": [
        {
          "dependencies": "\u0027:android-lib2:debugApiElements\u0027",
          "className": "com/codelang/android_lib2/TestCase"
        }
      ]
    }
  ]
}

```

- stringRef 中分析出 android-lib2 模块调用了 READ_EXTERNAL_STORAGE 权限
- methodRef 中分析出 android-lib2 与 androidx.core:core:1.7.0 模块调用了 NotificationManager.createNotificationChannel(NotificationChannel) 方法
- fieldRef 中分析出 android-lib2 模块调用了 ActivityThread.mCurDefaultDisplayDpi 字段