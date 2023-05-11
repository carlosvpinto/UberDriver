package com.carlosvicente.uberdriverkotlin.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlosvicente.uberdriverkotlin.adapters.HistoriesAdapter
import com.carlosvicente.uberdriverkotlin.adapters.HistoriesCancelAdapter
import com.carlosvicente.uberdriverkotlin.databinding.ActivityHistoriesDriverBinding
import com.carlosvicente.uberdriverkotlin.models.History
import com.carlosvicente.uberdriverkotlin.models.HistoryDriverCancel
import com.carlosvicente.uberdriverkotlin.providers.HistoryCancelProvider
import com.carlosvicente.uberdriverkotlin.providers.HistoryProvider
import com.tommasoberlose.progressdialog.ProgressDialogFragment

class HistoriesDriverCancelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoriesDriverBinding
    private var historyProvider = HistoryCancelProvider()
    private var histories = ArrayList<HistoryDriverCancel>()
    private lateinit var adapter: HistoriesCancelAdapter
    private var progressDialog = ProgressDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog.showProgressBar(this)
        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistoriesCancel.layoutManager = linearLayoutManager

        //CORREGIR EL ACTION BAR
        val actionBar = (this as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            // El tema actual utiliza ActionBar
            Log.d("TEMA", "ENTRO A TEMA CON ACTION VAR")
        } else {
            Log.d("TEMA", "ENTRO A TEMA SIN ACTION VAR")
            setSupportActionBar(binding.toolbar)
        }
        supportActionBar?.title = "Historial de viajes Cancelados"
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.BLACK)

        getHistories()
    }

    //OBTIENE LAS HISTORIAS
    private fun getHistories() {
        histories.clear()
        Log.d("HISTOCANCEL", "ENTRO A HISTORIA CANCEL FUNCION  $histories")
        historyProvider.getHistoriesCancel().get().addOnSuccessListener { query ->

            Log.d("HISTOCANCEL", "HISTORYDRIVERCANCELACTIVITY respuesta del getHistories $query")
            Log.d("HISTOCANCEL", "HISTORYDRIVERCANCELACTIVITY respuesta del getHistories:= ${query.documents.size} $query.documents")

            if (query != null) {
                if (query.documents.size > 0) {
                    val documents = query.documents

                    for (d in documents) {
                        var history = d.toObject(HistoryDriverCancel::class.java)
                        history?.id = d.id
                        histories.add(history!!)
                    }

                    adapter = HistoriesCancelAdapter(this@HistoriesDriverCancelActivity, histories)
                    binding.recyclerViewHistoriesCancel.adapter = adapter
                }
            }
            progressDialog.hideProgressBar(this)
        }
    }
}