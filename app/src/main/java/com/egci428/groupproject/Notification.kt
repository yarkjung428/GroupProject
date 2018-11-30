package com.egci428.groupproject

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.egci428.groupproject.MyService.Companion.CHANNEL_ID

class Notification: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Example Service Channel", NotificationManager.IMPORTANCE_DEFAULT);

            val manager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
}
