本地 module 与 GAV 转换插件，适用于使用壳工程改造的组件化，效果可查看 demo

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
        // 依赖 check 插件
        classpath "com.github.MRwangqi:modulePlugin:1.0.0"
    }
}
```

在 app 工程的 build.gradle 中依赖插件(app 为主壳模块)

```java
plugins {
    id 'com.android.application'
    // apply modulePlugin 插件
    id 'modulePlugin'
}
```


gradle.properties 配置

```
# GAV 转本地 module 开关
gav2project.enable=true
# 如果指定跳过模块，则不会参与 GAV 与 Project 的转换(仅在 gav2project.enable=true 时生效)
gav2project.skips=android-lib,android-lib
```


开启转换开关效果：
```
> Configure project :app
排除依赖:implementation com.codelang.library:android-lib:1.0.0
替换本地模块:implementation project(:android-lib)
```

开启跳过模块效果：
```
> Configure project :app
------- 跳过模块:android-lib----依赖为:com.codelang.library:android-lib--------
```