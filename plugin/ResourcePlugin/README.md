资源重复检查，目前仅支持：
- Drawable 目录重复资源检查
- Assets 目录重复资源检查
- layout 目录重复资源检查

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
        // 依赖 resourcePlugin 插件
        classpath "com.github.MRwangqi:resourcePlugin:1.0.0"
    }
}
```

在 app 工程的 build.gradle 中依赖插件(app 为主壳模块)

```java
plugins {
    id 'com.android.application'
    // apply resourcePlugin 插件
    id 'resourcePlugin'
}
```
示例 demo 运行效果：
![result](result.png)

