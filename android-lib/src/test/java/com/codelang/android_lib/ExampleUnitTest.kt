package com.codelang.android_lib

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun test2() {
        val str = """
plugins {
    id 'java-library'
    id 'kotlin'
    id 'uploadGithub'
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

 groupId = "com.aa.bb2"
  artifactId = "java_lib2"
  version = "1.0.02"
 
upload {
    //    githubURL = "git@github.com:MRwangqi/Maven.git"
    //    githubBranch = "apk"
    groupId = "com.aa.bb"
    artifactId = "java_lib"
    version = "1.0.0"
    //    githubURL = "git@github.com:MRwangqi/Maven.git"
    //    githubBranch = "apk"
    }
    
     groupId = "com.aa.bb3"
  artifactId = "java_lib3"
  version = "1.0.03"
  }
        """.trimIndent()


        Regex("upload\\s+\\{.+?groupId\\s*=\\s*\"(.+?)\".+?\\}",
                RegexOption.DOT_MATCHES_ALL)
                .find(str)?.groupValues?.let {
                    println(it[1])
                }

        Regex("upload\\s+\\{.+?artifactId\\s*=\\s*\"(.+?)\".+?\\}",
                RegexOption.DOT_MATCHES_ALL)
                .find(str)?.groupValues?.let {
                    println(it[1])
                }

        Regex("upload\\s+\\{.+?version\\s*=\\s*\"(.+?)\".+?\\}",
                RegexOption.DOT_MATCHES_ALL)
                .find(str)?.groupValues?.let {
                    println(it[1])
                }


//        Regex("groupId\\s*=\\s*\"(.+?)\".+artifactId\\s*=\\s*\"(.+?)\".+version\\s*=\\s*\"(.+?)\"",
//                RegexOption.DOT_MATCHES_ALL)
//                .find(str)?.groupValues?.let {
//                    println(it[1])
//                    println(it[2])
//                    println(it[3])
//                }
    }
}