package com.carlosvicente.uberdriverkotlin.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.activities.MapActivity
import com.carlosvicente.uberdriverkotlin.fragments.FragmenRecibir
import com.carlosvicente.uberdriverkotlin.fragments.ModalBottomSheetBooking

class servicios : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private val CHANNEL_ID = "com.carlosvicente.uberdriverkotlin"
    private val modalRecibir = FragmenRecibir()


    override fun onCreate() {
        super.onCreate()


        // Infla tu vista de superposición aquí
        Log.d("SUPERP", "DENTRO DE ONCREATE SUPERPOSICION")
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        this?.startActivity(i) 
        overlayView = LayoutInflater.from(this).inflate(R.layout.activity_map, null)

        // Configura el tamaño y la posición de la vista de superposición
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.NO_GRAVITY

        // Obtiene el WindowManager y agrega la vista de superposición
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayView, params)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //*****************

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TAXI AHORA")
                .setContentText("Solicitud de viaje Activa")
                .setSmallIcon(R.drawable.ic_little_person)
                .build()
            startForeground(1, notification)
            // Haz algo aquí


        //**********************
        mediaPlayer = MediaPlayer.create(this, R.raw.samsungtono)
        mediaPlayer?.start()

        return START_STICKY
    }


    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        super.onDestroy()
        // Remueve la vista de superposición cuando se destruye el servicio
       // windowManager.removeView(overlayView)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}

