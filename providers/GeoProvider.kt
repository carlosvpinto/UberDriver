package com.carlosvicente.uberdriverkotlin.providers

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore

class GeoProvider {

    val collection = FirebaseFirestore.getInstance().collection("Locations")
    val collectionMoto = FirebaseFirestore.getInstance().collection("LocationsMoto")
    val collectionWorking = FirebaseFirestore.getInstance().collection("LocationsWorking")
    val geoFirestore = GeoFirestore(collection)
    val geoFirestoreWorking = GeoFirestore(collectionWorking)
    val geoFirestoreMoto = GeoFirestore(collectionMoto)


    // GUARDA POSICION DEL CARRO
    fun saveLocation(idDriver: String, position: LatLng) {
        geoFirestore.setLocation(idDriver, GeoPoint(position.latitude, position.longitude))
    }

    //GUARDANDO POSICION DE MOTO
    fun saveLocationMoto(idDriver: String, position: LatLng){
           geoFirestoreMoto.setLocation(idDriver, GeoPoint(position.latitude,position.longitude))
    }

    fun removeLocationMoto(idDriver: String){
        collectionMoto.document(idDriver).delete()
    }


    fun saveLocationWorking(idDriver: String, position: LatLng) {
        geoFirestoreWorking.setLocation(idDriver, GeoPoint(position.latitude, position.longitude))
    }

    fun removeLocation(idDriver: String) {
        //geoFirestore.removeLocation(idDriver)
        collection.document(idDriver).delete()
    }
    fun removeLocationWorking(idDriver: String) {
        //geoFirestore.removeLocation(idDriver)
        collectionWorking.document(idDriver).delete()
    }

    fun getLocation(idDriver: String): Task<DocumentSnapshot> {
        return collection.document(idDriver).get().addOnFailureListener { exception ->
            Log.d("FIREBASE", "ERROR: ${exception.toString()}")
        }
    }
    fun getLocatioMoto(idDriver: String): Task<DocumentSnapshot> {
        return collectionMoto.document(idDriver).get().addOnFailureListener { exception ->
            Log.d("FIREBASE", "ERROR: ${exception.toString()}")
        }
    }



}