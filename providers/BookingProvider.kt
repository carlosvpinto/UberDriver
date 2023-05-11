package com.carlosvicente.uberdriverkotlin.providers

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.carlosvicente.uberdriverkotlin.models.Booking
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class BookingProvider {

    val db = Firebase.firestore.collection("Bookings")
    val authProvider = AuthProvider()
    val tablaBookings = FirebaseFirestore.getInstance().collection("Bookings")

    fun create(booking: Booking): Task<Void> {
        return db.document(authProvider.getId()).set(booking).addOnFailureListener {
            Log.d("FIRESTORE", "ERROR: ${it.message}")
        }
    }

    fun getBooking(): Query {
        Log.d("FIRESTORE", "VALOR DE AUTHPROVIDER: ${authProvider.getId()}")
        return db.whereEqualTo("idDriver", authProvider.getId())
    }
    fun getBookingINFO(): Query {
        Log.d("FIRESTORE", "VALOR DE AUTHPROVIDER: ${authProvider.getId()}")
        return db.whereEqualTo("idDriver", authProvider.getId()).whereEqualTo("status", "accept").orderBy("time",Query.Direction.DESCENDING).limit(1)
    }
    fun getBookingActivo(): Query {
        Log.d("FIRESTORE", "VALOR DE AUTHPROVIDER: ${authProvider.getId()}")
        return db.whereEqualTo("idDriver", authProvider.getId()).whereEqualTo("activo", true)
    }

    //BORRA EL BOOKING ****YO*************
    fun remove(): Task<Void> {
        return db.document(authProvider.getId()).delete().addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }


    fun updateStatus(idClient: String, status: String): Task<Void> {
        return db.document(idClient).update("status", status,).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }
    fun updateActivo(idClient: String, activo: Boolean): Task<Void> {
        return db.document(idClient).update("activo", activo).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }


}