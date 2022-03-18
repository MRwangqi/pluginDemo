### 一、依赖配置

配置 maven 镜像源和依赖
```java
buildscript {
    repositories {
        ...
        // 配上 maven 源
        maven{
           url "https://raw.githubusercontent.com/MRwangqi/Maven/main"
        }
    }
    dependencies {
        classpath "com.android.tools.build:gradle:7.0.4"
        // 依赖 check 插件
        classpath "com.github.MRwangqi:uploadPlugin:1.0.0"
    }
}
```

然后在模块工程的 build.gradle 中依赖插件:

```java
plugins {
    id 'com.android.library'
    // apply check 插件
    id 'uploadPlugin'
}

upload {
    groupId = ""
    artifactId = ""
    version = ""
    hasPomDepend = true
    url = "build/repo"
}
```

注意：
- 插件不支持 application 发组件
- 默认 hasPomDepend 为 true，可以不指定
- url 为指定的 aar 本地相对路径，不指定的话，默认为 build/repo 路径