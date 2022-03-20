package com.codelang.includebuildingdemo

import org.junit.Test

import org.junit.Assert.*
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)

        val githubURL = "git@github.com:MRwangqi/Maven.git"
        val lastIndex = githubURL.lastIndexOf("/")
        val lastIndex2 = githubURL.lastIndexOf(".git")

        println(githubURL.substring(lastIndex + 1, lastIndex2))
    }


}