package com.carlosvicente.uberdriverkotlin.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.firestore.ktx.toObject

import com.carlosvicente.uberdriverkotlin.databinding.ActivityCalificationClientBinding
import com.carlosvicente.uberdriverkotlin.databinding.ActivityMapTripBinding
import com.carlosvicente.uberdriverkotlin.models.History
import com.carlosvicente.uberdriverkotlin.providers.HistoryProvider
import com.tommasoberlose.progressdialog.ProgressDialogFragment

class CalificationClientActivity : AppCompatActivity() {

    private var history: History? = null
    private lateinit var binding: ActivityCalificationClientBinding
    private var extraPrice = 0.0
    private var historyProvider = HistoryProvider()
    private var calification = 0f
    private var progressDialog = ProgressDialogFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        progressDialog.showProgressBar(this)
        extraPrice = intent.getDoubleExtra("price", 0.0)
        binding.textViewPrice.text = "${String.format("%.2f", extraPrice)}$"

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, value, b ->
            calification = value
        }

        binding.btnCalification.setOnClickListener {
            if (history?.id != null) {
                updateCalification(history?.id!!)
            }
            else {
                Toast.makeText(this, "El id del historial es nulo", Toast.LENGTH_LONG).show()
            }
        }

        getHistory()
        progressDialog.hideProgressBar(this)
    }

    private fun updateCalification(idDocument: String) {
        progressDialog.showProgressBar(this)
        historyProvider.updateCalificationToClient(idDocument, calification).addOnCompleteListener {
            progressDialog.hideProgressBar(this)

            if (it.isSuccessful) {
                goToMap()
            }
            else {
                Toast.makeText(this@CalificationClientActivity, "Error al actualizar la calificacion", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToMap() {
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun getHistory() {
        Log.d("PRICE2", "ENTRO AL GETHISTORY: ${history?.toJson()}")
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->

            if (query != null) {
                Log.d("PRICE2", "DOCUMENTO PRICE: ${history?.toJson()}")
                if (query.documents.size > 0) {
                    history = query.documents[0].toObject(History::class.java)
                    history?.id = query.documents[0].id
                    binding.textViewOrigin.text = history?.origin
                    binding.textViewDestination.text = history?.destination
                    binding.textViewTimeAndDistance.text = "${history?.time} Min - ${String.format("%.1f", history?.km)} Km"

                    Log.d("PRICE2", "hISTORIAL: ${history?.toJson()}")
                }
                else {
                    Toast.makeText(this, "No se encontro el historial", Toast.LENGTH_LONG).show()
                }

            }

            progressDialog.hideProgressBar(this)
        }
    }
}