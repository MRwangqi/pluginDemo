package com.codelang.includebuildingdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.codelang.android_lib.AndroidLibActivity
import com.codelang.android_lib2.AndroidLib2Activity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClickLib1(view: View) {
        startActivity(Intent(this,AndroidLibActivity::class.java))
    }
    fun onClickLib2(view: View) {
        startActivity(Intent(this, AndroidLib2Activity::class.java))
    }
}