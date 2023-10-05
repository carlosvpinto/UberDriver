package com.carlosvicente.uberdriverkotlin.services

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
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
    var banderaActivaServi: Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    var sonidoAlarma = 0
    var contador= 0.0

    var booking:Booking? = null
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

        // Comprobar si la actividad de mapa está en primer plano
        verificarPlano()
//        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val appTask = activityManager.appTasks.firstOrNull()
//        appTask?.moveToFront()
//        val runningTaskInfo = activityManager.getRunningTasks(1)[0]
//        val currentActivity = runningTaskInfo.topActivity
        if (sonidoAlarma<1){
            musicaMediaPlayer()
        }

////        //*******************
//        Log.d("ESCUCHANDO", "ACTICIDAD PRIMER PLANO ANTES DEL IF $currentActivity currentActivity.packageName ${currentActivity?.packageName } packageName:$packageName  currentActivity.className: ${currentActivity?.className} MapActivity::class.java.name: ${MapActivity::class.java.name}")
//        if (currentActivity != null && currentActivity.packageName == packageName && currentActivity.className == MapActivity::class.java.name) {
//            // Si la actividad de mapa ya está en primer plano, no hacer nada
//            Log.d("ESCUCHANDO", "La actividad de mapa ya está en primer plano")
//        } else {
//            // Si la actividad de mapa no está en primer plano, abrirla
//            Log.d("ESCUCHANDO", "Abriendo la actividad de mapa  currentActivity $currentActivity currentActivity.packageName ${currentActivity?.packageName} currentActivity.className ${currentActivity?.className}")
//            val intent = Intent(this, MapActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//            //******************
//
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.REORDER_TASKS) == PackageManager.PERMISSION_GRANTED) {
//                // Si el permiso está concedido, mueve la tarea de tu aplicación a primer plano
//                Log.d("ESCUCHANDO", "PERMISO CONCEDIDO")
//                val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//                val appTask = activityManager.appTasks.firstOrNull()
//                appTask?.moveToFront()
//            } else {
//                // Si el permiso no está concedido, solicítalo al usuario
//                //   ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.REORDER_TASKS), PERMISSIONS_REQUEST_REORDER_TASKS)
//                Log.d("ESCUCHANDO", "PERMISO NO CONCEDIDO")
//            }
//
//        }

        return START_STICKY
    }

    //verificacion segundo plano segun google bard
    private fun verificarPlano(){
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses
        for (runningAppProcess in runningAppProcesses) {
            if (runningAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                // La aplicación está en primer plano
                Log.d("ESCUCHANDO", "FUNCION verificarPlano La aplicación está en PRIMER PLANO")
            }else{
                Log.d("ESCUCHANDO", "FUNCION verificarPlano La aplicación está en SEGUNDO PLANO")
                val appTask = activityManager.appTasks.firstOrNull()
                appTask?.moveToFront()
            }
        }
    }


    //PARA REPRODUCIR EL TONO
    //LLAMA AL TONO EN SEGUNDO PLANO ***
    fun musicaMediaPlayer(){
        mediaPlayer = MediaPlayer.create(this, R.raw.samsungtono)
        mediaPlayer?.start()
    }


    //LLAMA AL ACTIVITID DE NAVEGACION goToMapTrip ***YO******
    private fun goToMapTrip() {

        val i = Intent(this, MapActivity::class.java)

        i.putExtra("booking",  booking?.toJson())
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        super.onDestroy()
    }
}




