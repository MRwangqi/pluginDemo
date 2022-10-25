### 一、依赖配置



### 二、执行检查命令：

执行命令模板如下：
> ./gradlew classParse -Pbuild=${build variant}

要执行的 build variant 可以在 Android studio 中查看：
![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5a6f62c79ade4ae7864c572e852ee9b8~tplv-k3u1fbpfcp-zoom-1.image)

比如我们要检查 debug 的依赖分析，则命令如下：
> ./gradlew classParse -Pbuild=debug

当然，也可以直接使用如下命令进行检查，插件默认的 build variant 是 debug
> ./gradlew classParse




