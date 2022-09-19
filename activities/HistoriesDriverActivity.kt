package com.carlosvicente.uberdriverkotlin.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.adapters.HistoriesAdapter
import com.carlosvicente.uberdriverkotlin.adapters.HistoriesDriverAdapter
import com.carlosvicente.uberdriverkotlin.databinding.ActivityHistoriesBinding
import com.carlosvicente.uberdriverkotlin.databinding.ActivityHistoriesDriverBinding
import com.carlosvicente.uberdriverkotlin.databinding.ActivityHistoryDetailBinding
import com.carlosvicente.uberdriverkotlin.models.History
import com.carlosvicente.uberdriverkotlin.providers.HistoryDriverProvider
import com.carlosvicente.uberdriverkotlin.providers.HistoryProvider

class HistoriesDriverActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoriesDriverBinding
    private var historyProvider = HistoryDriverProvider()
    private var histories = ArrayList<History>()
    private lateinit var adapter: HistoriesDriverAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistories.layoutManager = linearLayoutManager

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Historial de viajes Cancelados"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)

        getHistories()
    }

    //OBTIENE LAS HISTORIAS
    private fun getHistories() {
        histories.clear()

        historyProvider.getHistories().get().addOnSuccessListener { query ->

            if (query != null) {
                if (query.documents.size > 0) {
                    val documents = query.documents

                    for (d in documents) {
                        var history = d.toObject(History::class.java)
                        history?.id = d.id
                        histories.add(history!!)
                    }

                    adapter = HistoriesDriverAdapter(this@HistoriesDriverActivity, histories)
                    binding.recyclerViewHistories.adapter = adapter
                }
            }

        }
    }
}