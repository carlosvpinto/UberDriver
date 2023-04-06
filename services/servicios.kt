package com.carlosvicente.uberdriverkotlin.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
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
import com.carlosvicente.uberdriverkotlin.activities.MapTripActivity
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
    private var bookingListener: ListenerRegistration? = null
    private val bookingProvider = BookingProvider()
    var bookingbandera: Booking? = null
    var bookingReserva: Booking? = null
    var banderaActiva: Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    var contador= 0.0
    private val modalBooking = ModalBottomSheetBooking()
    val timer = object: CountDownTimer(60000, 1000) {
        override fun onTick(counter: Long) {
            contador = (counter/ 1000).toDouble()
            // Obtener una referencia al Fragment


            Log.d("TIMER", "Counter: $counter")
        }

        override fun onFinish() {
            Log.d("TIMER", "ON FINISH")
            if (modalBooking.isAdded){
                modalBooking.dismiss()
            }


        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        // No se utiliza en este caso, retorna null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Llama a la función listenerBooking()
        listenerBooking()
        // Indica que el servicio debe seguir ejecutándose en segundo plano
        return START_STICKY
    }

    private fun listenerBooking() {
        // Aquí iría todo el código de la función listenerBooking()
        bookingListener = bookingProvider.getBooking().addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d("FIRESTORE", "ERROR: ${e.message}")
                return@addSnapshotListener
            }
            Log.d("ESCUCHANDO", "ERROR: ${snapshot?.documents?.size}")
            if (snapshot != null) {
                var CantBook = 0
                CantBook = snapshot.documents.size
                var Contador = 0
                if (snapshot.documents.size > 0) {
                    while (Contador < CantBook){

                        val booking = snapshot.documents[Contador].toObject(Booking::class.java)
                        Contador++
                        if (booking?.status == "create"){
                            bookingbandera = booking
                            bookingReserva = booking

                            //verica si esta activa la actividad
                            musicaMediaPlayer()
                                showModalBooking(booking!!)

                        }

                    }

                }
            }

        }
    }
    //LLAMA EL FRAGMENT**********************************************************************
    private fun showModalBooking(booking: Booking) {

        if (banderaActiva!= true) {
            val bundle = Bundle()
            bundle.putString("booking", booking.toJson())
            modalBooking.arguments = bundle
            modalBooking.isCancelable = false // NO PUEDA OCULTAR EL MODAL BOTTTOM SHEET

            goToMapTrip()

            //timer.start()
        } else {
            // La actividad no está activa
        }

    }

   private fun musicaMediaPlayer(){

        timer.start()
        mediaPlayer = MediaPlayer.create(this, R.raw.samsungtono)
        mediaPlayer?.start()
//            val serviceIntent = Intent(this, servicios::class.java)
//        startActivity(serviceIntent)

    }
    //LLAMA AL ACTIVITID DE NAVEGACION goToMapTrip ***YO******
    private fun goToMapTrip() {
        val i = Intent(this, MapTripActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }
}




