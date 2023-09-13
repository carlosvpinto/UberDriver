package com.carlosvicente.uberdriverkotlin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.carlosvicente.uberdriverkotlin.databinding.ActivityHistoryDetailCancelBinding
import com.carlosvicente.uberdriverkotlin.models.Driver
import com.carlosvicente.uberdriverkotlin.models.History
import com.carlosvicente.uberdriverkotlin.providers.*
import com.tommasoberlose.progressdialog.ProgressDialogFragment

class HistoryDetailCancelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryDetailCancelBinding

    private var historyProvider = HistoryProvider()
    private var driverProvider = DriverProvider()
    private var historyProviderCancel = HistoryCancelProvider()
    private var authProvider = AuthProvider()
    private var extraId = ""
    private var histories = ArrayList<History>()
    private var totalHistoriaRealizadas = 0
    private var progressDialog = ProgressDialogFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailCancelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        extraId = intent.getStringExtra("id")!!
        progressDialog.showProgressBar(this)
        getHistory()


        //CALCULA LA HISTORIAS
        CalcularHistorias()

        binding.imageViewBack.setOnClickListener { finish() }
    }

    private fun getHistory() {

        var TotalCancel = 0
        Log.d("HISTOCANCEL", "VALOR DEL DOCUMENTO: $extraId")
        histories.clear()

        historyProviderCancel.getHistoriesCancel().get().addOnSuccessListener { query ->

            TotalCancel= query.documents.size
            Log.d("HISTOCANCEL", "VALOR DEL TotalCancel: $TotalCancel")
            binding.textViewTotalCancel.text = TotalCancel.toString()

        }
        historyProviderCancel.getHistoryByIdCancel(extraId).addOnSuccessListener { document ->
            Log.d("HISTOCANCEL", "VALOR DEL DOCUMENTO: $document")
            if (document.exists()) {
                val history = document.toObject(History::class.java)
                getHistories()
                //binding.textViewMyPromedioCalification.text = "${history?.calificationToDriver}"
                getDriver()
            }

        }

    }

    //OBTIENE LOS DATOS DEL CONDUCTOR

    private fun getDriver() {
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()) {
                val driver = document.toObject(Driver::class.java)
                binding.textViewEmail.text = driver?.email
                binding.textViewName.setText(driver?.name + driver?.lastname)

                if (driver?.image != null) {
                    if (driver.image != "") {
                        Glide.with(this).load(driver.image).into(binding.circleImageProfile)
                    }
                }
            }
            progressDialog.hideProgressBar(this)
        }
    }

    //PARA CALCULAR EL NUMERO DE HISTORIAS CANCELADAS
    private fun CalcularHistorias() {
        histories.clear()

        historyProviderCancel.getHistoriesCancel().get().addOnSuccessListener { query ->
            query.documents.size


        }
    }
    private fun getHistories() {
        histories.clear()

        historyProvider.getHistories().get().addOnSuccessListener { query ->

            Log.d("HISTOCANCEL", " HistoryDetailCancelActivity respuesta del getHistories query $query")

            Log.d("HISTOCANCEL", "HistoryDetailCancelActivity respuesta del getHistories:= ${query.documents.size} $query.documents")

            if (query != null) {
                if (query.documents.size > 0) {

                    totalHistoriaRealizadas = query.documents.size

                    Log.d("HISTOCANCEL", "VALOR DEL HISTOCANCEL: $totalHistoriaRealizadas")


                    var promedioCalificacion = 0.0
                    val documents = query.documents
                    for (d in documents) {

                        var history = d.toObject(History::class.java)
                        Log.d("HISTOCANCEL", "VALOR DEL history?.calificationToDriver ${history?.calificationToDriver}")
                        if (history?.calificationToDriver != null){
                            promedioCalificacion += history?.calificationToDriver!!.toDouble()
                        }

                    }
                    Log.d("HISTOCANCEL", "VALOR DEL totalHistoriaRealizadas: $totalHistoriaRealizadas")
                    Log.d("HISTOCANCEL", "VALOR DEL promedioCalificacion: $promedioCalificacion")
                    Log.d("HISTOCANCEL", "VALOR DEL query.documents.size: ${query.documents.size}")

                    promedioCalificacion /= (query.documents.size)
                    binding.textViewRealizados.text = totalHistoriaRealizadas.toString()
                    binding.ratingBar.rating = promedioCalificacion.toFloat()
                    //binding.textViewMyPromedioCalification.text = promedioCalificacion.toString()
                    binding.textViewMyPromedioCalification.text = "${String.format("%.1f", promedioCalificacion)}"

                }
            }

        }
    }


}