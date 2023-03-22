package com.carlosvicente.uberdriverkotlin.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.activities.MapActivity
import com.carlosvicente.uberdriverkotlin.fragments.FragmenRecibir
import com.carlosvicente.uberdriverkotlin.fragments.ModalBottomSheetBooking
import com.carlosvicente.uberdriverkotlin.models.Booking
import com.carlosvicente.uberdriverkotlin.providers.BookingProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class service : Service() {

    private lateinit var bookingProvider: BookingProvider
    private var bookingListener: ListenerRegistration? = null
    var banderaActiva: Boolean = false
    var bookingbandera: Booking? = null
    var bookingReserva: Booking? = null
    private val handler = Handler()
    private var runnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }





    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runnable = object : Runnable {
            override fun run() {
                val miIntent = Intent(applicationContext, MapActivity::class.java)
                miIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                miIntent.action = "MI_ACCION"
                applicationContext.startActivity(miIntent)

                handler.postDelayed(this, 1000) // Llama a esta función cada 5 segundos
            }
        }
        handler.post(runnable as Runnable)
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        // Detener el listener de snapshot
        bookingListener?.remove()
    }
}
