## 一、配置 maven 镜像源和依赖
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
        // 依赖 upload 插件
        classpath "com.github.MRwangqi:uploadPlugin:1.0.0"
    }
}
```

## 二、依赖插件

目前插件支持两种方式上传：

### 1、上传到 github

在模块工程的 build.gradle 中依赖插件:

```java
plugins {
    id 'com.android.library'
    // 配置上传到 github
    id 'uploadGithub'
}

upload {
    // 必选
    groupId = ""
    // 必选
    artifactId = ""
    // 必选
    version = ""
}
```

插件在运行时会读取 local.properties 中 github 的内容

> local.properties 配置
```
# github 仓库链接(可选，如果不配置的话则发布到 project 下的 build/repo 目录)
# XXX 为填充区域
githubURL = XXX
# github 仓库分支(可选，如果不配置的话则以仓库当前配置的分支为准)
githubBranch = XXX
```


### 2、上传到 maven

在模块工程的 build.gradle 中依赖插件:

```java
plugins {
    id 'com.android.library'
     // 配置上传到 maven nexus
    id 'uploadMaven'
}

upload {
    // 必选
   groupId = ""
    // 必选
   artifactId = ""
    // 必选
   version = ""
}
```

插件在运行时会读取 local.properties 中 nexus 的内容

> local.properties 配置

```
   # nexus 地址 (可选，如果不配置的话则发布到 project 下的 build/repo 目录)
   # XXX 为填充区域
   nexusURL = XXX
   nexusName = XXX
   nexusPsw =  XXX
```


uploadMaven 也支持通过命令参数来输入：
>  ./gradlew :android-lib:upload -Pname=${nexusName} -Ppsw=${nexusPsw} -Purl=${nexusURL}

例如要打包 android-lib 模块，用户名和密码都是 admin，则命令行为:
> ./gradlew :android-lib:upload -Pname=admin -Ppsw=admin -Purl=http://localhost:8081/repository/android/



通用配置：
```
upload {
    // 模块下的依赖是否打入 pom : (可选，默认会打入)
    hasPomDepend = true
    // 模块是否打入 source 源码: (可选，默认会打入)
    sourceJar = true
}
```

