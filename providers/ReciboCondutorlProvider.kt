package com.carlosvicente.uberdriverkotlin.providers

import android.util.Log
import com.carlosvicente.uberdriverkotlin.models.ReciboConductor
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.carlosvicente.uberdriverkotlin.models.Booking
import com.carlosvicente.uberdriverkotlin.models.History

import com.google.firebase.firestore.DocumentSnapshot

class ReciboCondutorlProvider {

    val db = Firebase.firestore.collection("ReciboConductor")
    val authProvider = AuthProvider()

    fun crear(recibo: ReciboConductor): Task<DocumentReference> {
        return db.add(recibo).addOnFailureListener {
            Log.d("FIRESTORE", "ERROR: ${it.message}")
        }
    }

    fun getLastHistory(): Query { // CONSULTA COMPUESTA - INDICE
        return db.whereEqualTo("idClient", authProvider.getId()).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
    }

    fun getReciboConductor(): Query { // CONSULTA COMPUESTA - INDICE
        return db.whereEqualTo("idClient", authProvider.getId()).orderBy("timestamp", Query.Direction.DESCENDING)
    }

    fun getHistoryById(id: String): Task<DocumentSnapshot> {
        return db.document(id).get()
    }
    fun getReciboConductor(idDriver:String): Query { // CONSULTA COMPUESTA - INDICE
        return db.whereEqualTo("idDriver", idDriver).orderBy("timestamp", Query.Direction.DESCENDING)
    }


    fun getBooking(): Query {
        return db.whereEqualTo("idDriver", authProvider.getId())
    }



    }



