package com.carlosvicente.uberdriverkotlin.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.activities.MapActivity
import com.carlosvicente.uberdriverkotlin.activities.MapTripActivity
import com.carlosvicente.uberdriverkotlin.models.Booking
import com.carlosvicente.uberdriverkotlin.models.HistoryDriverCancel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

private lateinit var btnViajeEntrante: Button
private val modalRecibir = FragmenRecibir()

class FragmenRecibir : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fragmen_recibir, container, false)



        btnViajeEntrante = view.findViewById(R.id.btnViajeEntrante)




        btnViajeEntrante.setOnClickListener {
            Log.d("RECIBIR", "ENTRO A RECIBIR")
            dismiss()
            goToMapActivity()
        }



        return view
    }





    private fun goToMapActivity() {
        val i = Intent(context, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context?.startActivity(i)
    }

    companion object {
        const val TAG = "ModalViajeSheet"
    }


}