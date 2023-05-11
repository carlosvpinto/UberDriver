package com.carlosvicente.uberdriverkotlin.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.adapters.HistoriesAdapter
import com.carlosvicente.uberdriverkotlin.adapters.ReciboConductorAdapter
import com.carlosvicente.uberdriverkotlin.databinding.ActivityBilleteraBinding
import com.carlosvicente.uberdriverkotlin.databinding.ActivityHistoriesBinding
import com.carlosvicente.uberdriverkotlin.models.History
import com.carlosvicente.uberdriverkotlin.models.ReciboConductor
import com.carlosvicente.uberdriverkotlin.providers.AuthProvider
import com.carlosvicente.uberdriverkotlin.providers.HistoryProvider
import com.carlosvicente.uberdriverkotlin.providers.ReciboCondutorlProvider
import com.google.firebase.firestore.ListenerRegistration
import com.tommasoberlose.progressdialog.ProgressDialogFragment

class BilleteraConductorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBilleteraBinding
    private var reciboConductorProvider = ReciboCondutorlProvider()
    private var histories = ArrayList<History>()
    private lateinit var adapter:ReciboConductorAdapter

    private var progressDialog = ProgressDialogFragment
    private var reciboConductorListener: ListenerRegistration? = null
    private val authProvider = AuthProvider()
    private var isActivityVisible = false
    private var recibosCondutores = ArrayList<ReciboConductor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBilleteraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog.showProgressBar(this)
        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerDepositos.layoutManager = linearLayoutManager

        //CORREGIR EL ACTION BAR
        val actionBar = (this as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            // El tema actual utiliza ActionBar
            Log.d("TEMA", "ENTRO A TEMA CON ACTION VAR")
        } else {
            Log.d("TEMA", "ENTRO A TEMA SIN ACTION VAR")
            setSupportActionBar(binding.toolbar)
        }



        supportActionBar?.title = "Billetera Conductor"
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)

        listenerRecibosConductor()

    }

    //OBTIENE LAS HISTORIAS
    private fun listenerRecibosConductor(){
        //************************
        reciboConductorListener = reciboConductorProvider.getReciboConductor(authProvider.getId())
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("RecibosConductor", "Error al escuchar cambios en la base de datos", error)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && isActivityVisible && binding.recyclerDepositos.isVisible) {

                    recibosCondutores.clear() // Limpiar la lista antes de agregar los nuevos elementos

                    if (querySnapshot.documents.size > 0) {
                        val documents = querySnapshot.documents

                        for (d in documents) {
                            var pagoMovil = d.toObject(ReciboConductor::class.java)
                            pagoMovil?.id = d.id
                            recibosCondutores.add(pagoMovil!!)
                        }
                        adapter?.updatePagosRealizados(recibosCondutores)
                        adapter = ReciboConductorAdapter(this@BilleteraConductorActivity, recibosCondutores)
                        binding.recyclerDepositos.adapter = adapter
                        progressDialog.hideProgressBar(this)
                        //adapter?.notifyDataSetChanged() // Notificar al adaptador después de asignar la nueva lista

                    }else{
                        Toast.makeText(this, "Sin deposito", Toast.LENGTH_SHORT).show()
                        progressDialog.hideProgressBar(this)
                    }



                }else{
                    Toast.makeText(this, "No tiene ningun deposito", Toast.LENGTH_SHORT).show()
                    progressDialog.hideProgressBar(this)
                }
            }
    }


    override fun onBackPressed() {
        goToMapActivity()
        super.onBackPressed()
    }
    override fun onDestroy() {
        super.onDestroy()

        // Eliminar el listener de Firebase cuando se destruye la actividad
        reciboConductorListener?.remove()
        reciboConductorListener = null
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
    }
    private fun goToMapActivity() {
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)

    }
}
