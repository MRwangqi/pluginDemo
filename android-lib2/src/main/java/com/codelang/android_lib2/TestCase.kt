package com.codelang.android_lib2


import android.Manifest
import android.app.Activity
import android.app.ActivityThread
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

@RequiresApi(Build.VERSION_CODES.O)
class TestCase(val context: Context) {

    private var channel: NotificationChannel? = null

    private val notificationManager by lazy {
        val name = "channel_name"
        val descriptionText = "channel_description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel("CHANNEL_ID", name, importance)
        mChannel.description = descriptionText
        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * 广播注册于反注册的检查
     */
    fun testRegisterReceiver(context: Context) {
        context.registerReceiver(null, null)

        context.unregisterReceiver(null)
    }

    /**
     * 推送渠道注册
     */
    fun testPush(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "channel_name"
            val descriptionText = "channel_description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("CHANNEL_ID", name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    /**
     * 权限注册
     */
    fun testPermission(context: Activity) {
        // 针对附近 Wi-Fi 设备的新运行时权限
        ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)

        // 细化的媒体权限
        ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)

        // 在后台使用身体传感器需要新的权限
        ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.BODY_SENSORS), 0)
    }

    fun nonSdk() {
        val thread= ActivityThread()
        thread.mCurDefaultDisplayDpi
    }
}