package com.carlosvicente.uberdriverkotlin.providers

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.carlosvicente.uberdriverkotlin.models.Booking
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference

import com.google.firebase.firestore.*

class BookingProvider {

    val db = Firebase.firestore.collection("Bookings")
    val authProvider = AuthProvider()
    val tablaBookings = FirebaseFirestore.getInstance().collection("Bookings")

    fun create(booking: Booking): Task<Void> {
        return db.document(authProvider.getId()).set(booking).addOnFailureListener {
            Log.d("FIRESTORE", "ERROR: ${it.message}")
        }
    }
    fun getBookingByCollectionKey(collectionKey: String): Query {
        return db.whereEqualTo(FieldPath.documentId(), collectionKey)
    }

    fun getBooking1(): Query {
        return db.whereEqualTo("idDriver", authProvider.getId())
    }
    fun getBooking2(): Query {
        return db.whereEqualTo("idDriver2", authProvider.getId())
    }
    fun getBooking3(): Query {
        return db.whereEqualTo("idDriver3", authProvider.getId())
    }


    fun getBooking(): Query {
        return db.whereEqualTo("idDriver", authProvider.getId())

    }
    fun getBookingAsignando(): Query {
        return db.whereEqualTo("idDriverAsig", authProvider.getId())

    }




    fun getBookingINFO(): Query {
        return db.whereEqualTo("idDriver", authProvider.getId()).whereEqualTo("status", "accept").orderBy("time",Query.Direction.DESCENDING).limit(1)
    }
    fun getBookingActivo(): Query {
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

    fun updateAsignado(idClient: String, idDriverAsig: String, status: String, asignado: Boolean): Task<Void> {
        val updates = hashMapOf<String, Any>(
            "idDriverAsig" to idDriverAsig,
            "status" to  status,
            "asignado" to asignado
        )

        return db.document(idClient).update(updates)
            .addOnFailureListener { exception ->
                Log.d("FIRESTORE", "ERROR: ${exception.message}")
            }
    }


}