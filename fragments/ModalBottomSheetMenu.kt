package com.carlosvicente.uberdriverkotlin.fragments


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.toObject
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.activities.*
import com.carlosvicente.uberdriverkotlin.models.Booking
import com.carlosvicente.uberdriverkotlin.models.Driver
import com.carlosvicente.uberdriverkotlin.providers.AuthProvider
import com.carlosvicente.uberdriverkotlin.providers.BookingProvider
import com.carlosvicente.uberdriverkotlin.providers.DriverProvider
import com.carlosvicente.uberdriverkotlin.providers.GeoProvider
import com.example.easywaylocation.EasyWayLocation
import com.google.android.gms.maps.model.LatLng

class ModalBottomSheetMenu: BottomSheetDialogFragment() {
    private var myLocationLatLng: LatLng? = null
    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()
    var easyWayLocation: EasyWayLocation? = null
    var textViewUsername: TextView? = null
    var linearLayoutLogout: LinearLayout? = null
    var linearLayoutProfile: LinearLayout? = null
    var linearLayoutHistory: LinearLayout? = null
    var linearLayoutBilletera: LinearLayout? = null
    var linearLayoutHistoryCancel: LinearLayout? = null
    var linearlayautGanaciaTotal:LinearLayout?=null

    // PROVANDO PARA SALIR DE LA LOCALIXACION
    val geoProvider = GeoProvider()

    //Verifica si es moto
    private var isMotoTrip = true


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_sheet_menu, container, false)

        textViewUsername = view.findViewById(R.id.textViewUsername)

        linearLayoutLogout = view.findViewById(R.id.linearLayoutLogout)
        linearLayoutBilletera = view.findViewById(R.id.linearLayoutBilletera)
        linearLayoutProfile = view.findViewById(R.id.linearLayoutProfile)
        linearLayoutHistory = view.findViewById(R.id.linearLayoutHistory)
        linearLayoutHistoryCancel = view.findViewById(R.id.linearLayoutHistoryCancel)
        linearlayautGanaciaTotal = view.findViewById(R.id.linearLayoutResumeGana)


        getDriver()

        linearLayoutLogout?.setOnClickListener { goToMain() }
        linearLayoutBilletera?.setOnClickListener { goToBilletera() }
        linearLayoutProfile?.setOnClickListener { goToProfile() }
        linearLayoutHistory?.setOnClickListener { goToHistories() }
        linearLayoutHistoryCancel?.setOnClickListener { goToHistoriesCancel()}
        linearlayautGanaciaTotal?.setOnClickListener{gotoGanancia()}
        return view
    }

    private fun goToBilletera() {
        val i = Intent(activity, BilleteraConductorActivity::class.java)
        startActivity(i)
    }

    private fun gotoGanancia() {
        val i = Intent(activity, GananciasActivity::class.java)
        startActivity(i)
    }

    private fun goToProfile() {
        val i = Intent(activity, ProfileActivity::class.java)
        startActivity(i)
    }

    private fun goToHistories() {
        val i = Intent(activity, HistoriesActivity::class.java)
        startActivity(i)
    }

    //HISTORIAS CANCELADAS (YO)*************************************
    private fun goToHistoriesCancel() {
        val i = Intent(activity, HistoriesDriverCancelActivity::class.java)
        startActivity(i)
    }

    private fun goToMain() {

        salirEliminando()
        easyWayLocation?.endUpdates()


        authProvider.logout()

        val i = Intent(activity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    //PARA ELIMINAR AL CONDUCTOR DE LA LOCALIZACION
    private fun salirEliminando(){

        //ELIMINA LA DISPONIBILIDAD DEL CONDUCTOR*****YO****
        if ( authProvider.getId()!= null) {
            if (authProvider.getId()!= ""){
                geoProvider.removeLocation(authProvider.getId())
            }
        }
        // DESCONECTAR MOTO
        if (authProvider.getId()!= null){
            geoProvider.removeLocationMoto(authProvider.getId())
        }
        easyWayLocation?.endUpdates()

    }
    private fun getDriver() {
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()) {
                val driver = document.toObject(Driver::class.java)
                textViewUsername?.text = "${driver?.name} ${driver?.lastname}"
            }
        }
    }

    // VERIFICA SI ES CARRO O MOTO YO************************
    private fun SaberSiesMoto(){
        if (authProvider.getId()!= "") {
            driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
                if (document.exists()) {
                    val driver = document.toObject(Driver::class.java)

                    if (driver?.tipo.toString() == "Carro") {
                        isMotoTrip = false
                    }
                    if (driver?.tipo.toString() == "Moto") {
                        isMotoTrip = true
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "ModalBottomSheetMenu"
    }



}