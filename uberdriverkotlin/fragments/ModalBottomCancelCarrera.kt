package com.carlosvicente.uberdriverkotlin.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.activities.MapActivity
import com.carlosvicente.uberdriverkotlin.models.Booking
import com.carlosvicente.uberdriverkotlin.models.HistoryDriverCancel
import com.carlosvicente.uberdriverkotlin.providers.AuthProvider
import com.carlosvicente.uberdriverkotlin.providers.BookingProvider
import com.carlosvicente.uberdriverkotlin.providers.HistoryCancelProvider
import kotlinx.android.synthetic.main.fragment_modal_bottom_cancel_carrera.*
import java.util.*
import kotlin.math.log


private lateinit var booking: Booking
var countDownTimerbooki: CountDownTimer? = null
private val bookingProvider = BookingProvider()
private val authProvider = AuthProvider()
private val historyCancelProvider = HistoryCancelProvider()

class ModalBottomCancelCarrera : Fragment() {

     var name: String? = null
     var address: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(NAME_BUNDLE)
            address = it.getString(ADDRESS_BUNDLE)
            Log.d("aris", "name ${name.orEmpty()} ")
            //log.i("tag",name.orEmpty())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_modal_bottom_cancel_carrera, container, false)
        val data = arguments?.getString("booking")
        booking = Booking.fromJson(data!!)!!

        btnCancelarComenzado.setOnClickListener{cancelBooking(booking?.idClient!!)

        }
    }

    companion object {
        var NAME_BUNDLE= "name_bundle"
        var ADDRESS_BUNDLE=  "address_bundle"

        fun newInstance(name: String, address: String) =
            ModalBottomCancelCarrera().apply {
                arguments = Bundle().apply {
                    putString(NAME_BUNDLE, name)
                    putString(ADDRESS_BUNDLE, address)
                }
            }
    }

    fun cancelBooking(idClient: String) {
        countDownTimerbooki?.cancel()
        bookingProvider.updateStatus(idClient, "cancel").addOnCompleteListener {
            (activity as MapActivity).musicaMediaPlayerStop()
            (activity as? MapActivity)?.timer?.cancel()
            createHistoryCancel()//CREA HISTORIA DE BOOKING CANCELADOS*******************
            //dismiss()
            (activity as? MapActivity)?.disconnectDriver()
            (activity as? MapActivity)?.bookingbandera = null

        }
    }
    //CREA HISTORIA DE BOOKING CANCELADOS!!!!**************************
    private fun createHistoryCancel() {
        Log.d("PRICE", "VALOR DE TOTAL  ")
        val historyCancel = HistoryDriverCancel(
            idDriver = authProvider.getId(),
            idClient = booking?.idClient,
            origin = booking?.origin,
            destination = booking?.destination,
            originLat = booking?.originLat,
            originLng = booking?.originLng,
            destinationLat = booking?.destinationLat,
            destinationLng = booking?.destinationLng,
            timestamp = Date().time,
            causa = "Cancelada por el Conductor",
            fecha = Date()
        )
        historyCancelProvider.create(historyCancel).addOnCompleteListener {
            if (it.isSuccessful) {

                Log.d("HISTOCANCEL", "LA HISTORIA DE CANCEL $historyCancel ")

            }
        }
    }
}

