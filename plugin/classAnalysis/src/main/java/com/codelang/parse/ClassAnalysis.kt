package com.codelang.parse

import classfile.ClassFile
import classfile.ConstantPool
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.codelang.parse.bean.ConfigBean
import com.codelang.parse.bean.ResponseBean
import com.codelang.parse.extension.ConfigFileExtension
import com.google.gson.Gson
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.jar.JarFile

class ClassAnalysis : Plugin<Project> {

    companion object {
        const val EXT_NAME = "classAnalysis"
        const val BUILD = "build"
    }

    private val stringMap = hashMapOf<String, ArrayList<ResponseBean.Ref>>()
    private val methodMap = hashMapOf<ConfigBean.Method, ArrayList<ResponseBean.Ref>>()
    private val fieldMap = hashMapOf<ConfigBean.Feild, ArrayList<ResponseBean.Ref>>()


    override fun apply(project: Project) {
        // ./gradlew classAnalysis -Pbuild=debug
        val params = project.gradle.startParameter.projectProperties

        val build = if (params.containsKey(BUILD)) {
            params[BUILD] ?: "debug"
        } else {
            // 默认 debug 兜底
            "debug"
        }

        project.extensions.create(EXT_NAME, ConfigFileExtension::class.java)

        project.afterEvaluate {
            val extension = project.extensions.findByName(EXT_NAME) as ConfigFileExtension
            val configFile = extension.configFile
            if (configFile.isEmpty()) {
                throw IllegalArgumentException("configFilePath 配置为空，请检查是否配置")
            }
            val file = File(configFile)

            val configBean = if (file.exists()) {
                if (configFile.endsWith(".json")) {
                    Gson().fromJson(file.readText(), ConfigBean::class.java)
                } else {
                    throw IllegalArgumentException("configFilePath 配置文件必须是 json 文件")
                }
            } else {
                throw IllegalArgumentException("configFilePath 配置文件不存在")
            }


            val configurationName = build + "CompileClasspath"
            project.tasks.create("classAnalysis") {
                it.doLast {
                    val resolvableDeps = project.configurations.getByName(configurationName).incoming
                    val view = resolvableDeps.artifactView { conf ->
                        conf.attributes { attr ->
                            attr.attribute(AndroidArtifacts.ARTIFACT_TYPE, AndroidArtifacts.ArtifactType.CLASSES_JAR.type)
                        }
                    }

                    // 获取依赖 aar transform 的 jar  路径
                    view.artifacts.forEachIndexed { index, result ->
                        val dep = result.variant.displayName.split(" ").find { it.contains(":") }
                                ?: result.variant.displayName
                        println("index=$index dep=$dep")
                        val f = result.file
                        unzipJar(dep, f, configBean)
                    }

                    // 生成文件
                    generatorFile(project)
                }
            }
        }
    }


    private fun unzipJar(dep: String, file: File, configBean: ConfigBean) {
        // 获取 jar 中的 class 文件
        val jarFile = JarFile(file, false, JarFile.OPEN_READ)
        val jarEntries = jarFile.entries()
        while (jarEntries.hasMoreElements()) {
            val entry = jarEntries.nextElement()
            if (!entry.isDirectory && entry.name.endsWith(".class") && !entry.name.endsWith("module-info.class")) {
                var ins: InputStream? = null
                try {
                    ins = jarFile.getInputStream(entry)
                    val cf = ClassFile.read(ins)
                    parseClass(dep, cf, configBean)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (ins != null) {
                        try {
                            ins.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }


    private fun parseClass(dep: String, cf: ClassFile, configBean: ConfigBean) {
//        println("className=" + cf.name + "------------------")
        cf.constant_pool.entries().forEach {
            when (it) {
                is ConstantPool.CONSTANT_String_info -> {
                    dealString(it, configBean, dep, cf)
                }
                is ConstantPool.CONSTANT_Methodref_info -> {
                    dealMethod(it, configBean, dep, cf)
                }
                is ConstantPool.CONSTANT_Fieldref_info -> {
                    dealField(it, configBean, dep, cf)
                }
            }
        }
    }


    private fun dealString(info: ConstantPool.CONSTANT_String_info, configBean: ConfigBean, dep: String, cf: ClassFile) {
        val string = info.string
//        println("CONSTANT_String_info =$string  string_index=${info.string_index}")

        // 检查常量是否在申明的权限中
        if (configBean.stringRef.contains(string)) {
            // 记录权限引用
            var list = stringMap[string]
            if (list == null) {
                list = ArrayList()
            }
            list.add(ResponseBean.Ref().apply {
                dependencies = dep
                className = cf.name
            })
            stringMap[string] = list
        }
    }


    private fun dealMethod(info: ConstantPool.CONSTANT_Methodref_info, configBean: ConfigBean, dep: String, cf: ClassFile) {
        val methodName = info.nameAndTypeInfo.name
        val methodType = info.nameAndTypeInfo.type
        val clazzName = info.classInfo.name
//        println("CONSTANT_Methodref_info name=$methodName type=$methodType className=${clazzName}")

        configBean.methodRef.find {
            it.className == clazzName &&
                    if (it.method.isNullOrEmpty()) {
                        true
                    } else {
                        it.method == methodName
                    } &&
                    // 如果申明的 signature 为空的话，则全匹配
                    if (it.signature.isNullOrEmpty()) {
                        true
                    } else {
                        it.signature == methodType
                    }
        }?.let {
            var list = methodMap[it]
            if (list == null) {
                list = ArrayList()
            }
            list.add(ResponseBean.Ref().apply {
                dependencies = dep
                className = cf.name
            })
            methodMap[it] = list
        }

    }


    private fun dealField(info: ConstantPool.CONSTANT_Fieldref_info, configBean: ConfigBean, dep: String, cf: ClassFile) {
        val fieldName = info.nameAndTypeInfo.name
        val filedType = info.nameAndTypeInfo.type
        val clazzName = info.className
//        println("CONSTANT_Fieldref_info name=$filedName type=$filedType class=${info.className}")

        configBean.fieldRef.find {
            it.className == clazzName &&
                    if (it.fieldName.isNullOrEmpty()) {
                        true
                    } else {
                        it.fieldName == fieldName
                    } &&
                    if (it.signature.isNullOrEmpty()) {
                        true
                    } else {
                        it.signature == filedType
                    }
        }?.let {
            var list = fieldMap[it]
            if (list == null) {
                list = ArrayList()
            }
            list.add(ResponseBean.Ref().apply {
                dependencies = dep
                className = cf.name
            })
            fieldMap[it] = list
        }
    }

    private fun generatorFile(project: Project) {
        // 转换
        val response = ResponseBean()
        response.stringRef = stringMap.map { entry ->
            val p = ResponseBean.Permission()
            p.name = entry.key
            p.ref = entry.value
            p
        }.toList()

        response.fieldRef = fieldMap.map { entry ->
            val p = ResponseBean.Field()
            p.className = entry.key.className
            p.fieldName = entry.key.fieldName
            p.signature = entry.key.signature
            p.ref = entry.value
            p
        }.toList()

        response.methodRef = methodMap.map { entry ->
            val p = ResponseBean.Method()
            p.className = entry.key.className
            p.method = entry.key.method
            p.signature = entry.key.signature
            p.ref = entry.value
            p
        }.toList()

        // 生成文件
        val text = Gson().toJson(response)
        if (!project.buildDir.exists()) {
            project.buildDir.mkdir()
        }
        val outputFile = File(project.buildDir.absolutePath + File.separator + "classAnalysis.json")
        outputFile.writeText(text)
    }
}