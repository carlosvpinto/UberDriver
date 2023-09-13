package com.carlosvicente.uberdriverkotlin.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, MyReceiver::class.java)
        context.startService(serviceIntent)
    }
}

