package com.carlosvicente.uberdriverkotlin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.databinding.ActivityGananciasBinding
import com.carlosvicente.uberdriverkotlin.databinding.ActivityHistoryDetailCancelBinding
import com.carlosvicente.uberdriverkotlin.models.Driver
import com.carlosvicente.uberdriverkotlin.models.History
import com.carlosvicente.uberdriverkotlin.providers.AuthProvider
import com.carlosvicente.uberdriverkotlin.providers.DriverProvider
import com.carlosvicente.uberdriverkotlin.providers.HistoryCancelProvider
import com.carlosvicente.uberdriverkotlin.providers.HistoryProvider
import com.carlosvicente.uberdriverkotlin.utils.RelativeTime
import com.tommasoberlose.progressdialog.ProgressDialogFragment

class GananciasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGananciasBinding

    private var historyProvider = HistoryProvider()
    private var driverProvider = DriverProvider()
    private var historyProviderCancel = HistoryCancelProvider()
    private var authProvider = AuthProvider()
    private var extraId = ""
    private var histories = ArrayList<History>()
    private var totalHistoriaRealizadas = 0
    private var progressDialog = ProgressDialogFragment
    private var totalViajes = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGananciasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        getHistory()

        //CALCULA LA HISTORIAS
        CalcularHistorias()

        binding.imageViewBack.setOnClickListener { finish() }
    }

    private fun getHistory() {

        var TotalCancel = 0

        histories.clear()

        historyProvider.getHistories().get().addOnSuccessListener { query ->

            totalViajes= query.documents.size
            binding.txtNroViajes.text = totalViajes.toString()
        }

                getHistories()
                getDriver()
    }

    //OBTIENE LOS DATOS DEL CONDUCTOR

    private fun getDriver() {
        var validandoImagen: Boolean = true
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()) {
                val driver = document.toObject(Driver::class.java)
                binding.txtEMailConduc.text = driver?.email
                binding.txtNombreConductor.setText(driver?.name + driver?.lastname)


                //validando la imagen
                val imageUrl = driver?.image
                if (imageUrl.isNullOrEmpty()) {
                    Log.e("GananciasActivity", "Image URL is null or empty")
                    Toast.makeText(this, "Sin imagen", Toast.LENGTH_SHORT).show();
                    validandoImagen = false
                }


                val extension = imageUrl?.substringAfterLast(".")
                if (extension.equals("png", ignoreCase = true) &&
                    extension.equals("jpg", ignoreCase = true) &&
                    extension.equals("jpeg", ignoreCase = true) &&
                    extension.equals("gif", ignoreCase = true)) {
                    Toast.makeText(this, "Extension de imagen Incorrecta", Toast.LENGTH_LONG).show();
                    Log.e("GananciasActivity", "Extension Incorrecta image format: $extension")
                    validandoImagen = false
                }


               // Glide.with(this).load(imageUrl).into(binding.circleImageProfile)

                if (driver?.image != null && validandoImagen== true) {
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
        var TotalGanado = 0.0
        histories.clear()
        Log.d("HISTOGANANCIAS", " ENTRA A GETHISTORIAS")
        historyProvider.getHistories().get().addOnSuccessListener { query ->

            if (query != null) {
                if (query.documents.size > 0) {

                    totalHistoriaRealizadas = query.documents.size


                    var promedioCalificacionClient = 0.0
                    var promedioCalificacion = 0.0
                    val documents = query.documents
                    for (d in documents) {

                        var history = d.toObject(History::class.java)
                        if (history?.calificationToDriver != null){
                            promedioCalificacion += history?.calificationToDriver!!.toDouble()
                        }
                        if (history?.price!=null){
                            TotalGanado += history?.price!!.toDouble()
                        }
                        if (history?.calificationToClient!=null){
                            promedioCalificacionClient += history?.calificationToClient!!.toDouble()
                        }

                    }


                    promedioCalificacion /= (query.documents.size)
                    promedioCalificacionClient/=(query.documents.size)
                    var totalCon2 = TotalGanado
                    binding.txtTotalGanancias.text = String.format("%.1f", totalCon2)
                   // binding.textViewPrice.text = "${String.format("%.1f", history?.price)}$"
                    //binding.txtTotalGanancias.text = TotalGanado.toString()
                   // binding.txtNombreConductor.text = promedioCalificacion.toString()
                    binding.ratingBarCliente.rating= promedioCalificacionClient.toFloat()
                    //binding.textViewMyPromedioCalification.text = promedioCalificacion.toString()
                    //binding.txtPromedioObtenida.text = "${String.format("%.1f", promedioCalificacion)}"
                    binding.ratingBar.rating = promedioCalificacion.toFloat()

                }
            }

        }
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->
            if (query!=null){
                Log.d("GANANCIA", "VALOR QUERY: ${query}")
                if (query.size()>0){
                    var historia = query.documents[0].toObject(History::class.java)
                    binding.txtfechaPViaje.text = RelativeTime.getTimeAgo(historia?.timestamp!!, this@GananciasActivity)
                }

            }
        }
    }
}