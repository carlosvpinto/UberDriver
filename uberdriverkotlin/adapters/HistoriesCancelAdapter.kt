package com.carlosvicente.uberdriverkotlin.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.activities.HistoryDetailCancelActivity
import com.carlosvicente.uberdriverkotlin.activities.HistoriesDriverCancelActivity
//import com.carlosvicente.uberdriverkotlin.activities.HistoriesDetailActivity
import com.carlosvicente.uberdriverkotlin.models.HistoryDriverCancel
import com.carlosvicente.uberdriverkotlin.utils.RelativeTime

//import com.carlosvicente.uberdriverkotlin.utils.RelativeTime

class HistoriesCancelAdapter(val context: Activity, val histories: ArrayList<HistoryDriverCancel>): RecyclerView.Adapter<HistoriesCancelAdapter.HistoriesAdapterViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriesAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_history, parent, false)
        return HistoriesAdapterViewHolder(view)
    }

    // ESTABLECER LA INFORMACION
    override fun onBindViewHolder(holder: HistoriesAdapterViewHolder, position: Int) {

        val history =  histories[position] // UN SOLO HISTORIAL
        holder.textViewOrigin.text = history.origin
        holder.textViewDestination.text = history.destination
        if (history.timestamp != null) {
            holder.textViewDate.text = RelativeTime.getTimeAgo(history.timestamp!!, context)
        }

        holder.itemView.setOnClickListener { goToDetailCancel(history?.id!!) }
    }

    private fun goToDetailCancel(idHistory: String) {
        val i = Intent(context, HistoryDetailCancelActivity::class.java)
        i.putExtra("id", idHistory)
        context.startActivity(i)
    }

    // EL TAMAñO DE LA LISTA QUE VAMOS A MOSTRAR
    override fun getItemCount(): Int {
        return histories.size
    }


    class HistoriesAdapterViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val textViewOrigin: TextView
        val textViewDestination: TextView
        val textViewDate: TextView

        init {
            textViewOrigin = view.findViewById(R.id.textViewOrigin)
            textViewDestination = view.findViewById(R.id.textViewDestination)
            textViewDate = view.findViewById(R.id.textViewDate)
        }

    }


}