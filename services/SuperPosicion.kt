package com.carlosvicente.uberdriverkotlin.services

import android.app.*
import android.app.Service.START_STICKY
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.activities.MapActivity

class SuperPosicion : Service(){
    private val NOTIFICATION_ID = 1
    private val NOTIFICATION_CHANNEL_ID = "MyChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        // Do your background work here
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MapActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("My Service")
            .setContentText("Running in the background...")
            .setSmallIcon(R.drawable.icon_taxi)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}
