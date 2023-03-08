package com.carlosvicente.uberdriverkotlin.services

import android.content.BroadcastReceiver
import android.content.Context

import android.content.Intent
import android.media.MediaPlayer
import android.util.Log

import com.carlosvicente.uberdriverkotlin.R
private var mediaPlayer: MediaPlayer? = null
val TAG = "ALARMA"
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "PASO POR LA ALARMA")

    }
}