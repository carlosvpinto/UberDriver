package com.carlosvicente.uberdriverkotlin.fragments


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.activities.MapActivity
import com.carlosvicente.uberdriverkotlin.activities.MapTripActivity
import com.carlosvicente.uberdriverkotlin.models.Booking
import com.carlosvicente.uberdriverkotlin.models.Client
import com.carlosvicente.uberdriverkotlin.models.Driver
import com.carlosvicente.uberdriverkotlin.models.HistoryDriverCancel
import com.carlosvicente.uberdriverkotlin.providers.*
import com.ekn.gruzer.gaugelibrary.Range
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import kotlinx.android.synthetic.main.modal_bottom_sheet_booking.*
import java.util.*

class ModalBottomSheetBooking: BottomSheetDialogFragment() {


    private lateinit var textViewOrigin: TextView
    private lateinit var textViewDestination: TextView
    private lateinit var textViewTimeAndDistance: TextView
    private lateinit var textViewTipoPago: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnCancel: Button
    private lateinit var txtTiempo: TextView
    private var progressDialog = ProgressDialogFragment




    private val bookingProvider = BookingProvider()
    private val historyCancelProvider = HistoryCancelProvider()
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private lateinit var booking: Booking
    lateinit var reservaCliente: Booking
    private lateinit var relojllamar: ImageView

    //para el reloj
    private val clienteProvider = ClientProvider()
    private var idDriver = ""
    private var cliente: Client? = null
    private var inicioTime = 0.0
    var countDownTimerbooki: CountDownTimer? = null




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_sheet_booking, container, false)
        //PARA INICIAR EL RELOJ EN EL TIEMPO CORRECTO******************************
        val myActivity = activity as MapActivity // Cast a la actividad
        val inicioReloj = myActivity.contador
        activartiempo(inicioReloj.toLong()*1000)



        textViewOrigin = view.findViewById(R.id.textViewOrigin)
        textViewDestination = view.findViewById(R.id.textViewDestination)
        textViewTimeAndDistance = view.findViewById(R.id.textViewTimeAndDistance)
        textViewTipoPago = view.findViewById(R.id.txtTipoPago)


        btnAccept = view.findViewById(R.id.btnAccept)
        btnCancel = view.findViewById(R.id.btnCancel)


        //  RECIBE EL BOOKING DE LA ACTIVITY MAPACTIVITY
        val data = arguments?.getString("booking")
        booking = Booking.fromJson(data!!)!!

        reservaCliente = booking
        Log.d("ARGUMENTS", "Booking: ${booking?.toJson()}")

        textViewOrigin.text = booking?.origin
        textViewDestination.text = booking?.destination
        textViewTimeAndDistance.text = "${String.format("%.1f", booking?.time)} Min - ${String.format("%.1f", booking?.km)} Km"
        textViewTipoPago.text= booking?.tipoPago



        btnAccept.setOnClickListener { acceptBooking(booking?.idClient!!) }
        btnCancel.setOnClickListener { cancelBooking(booking?.idClient!!) }
       // relojllamar.setOnClickListener{configurarGaude()}
        getClientInfo(booking?.idClient!!)
       // configurarGaude()
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val range = Range()
        range.color = Color.parseColor("#ce0000")
        range.from = 0.0
        range.to = 10.0

        val range2 = Range()
        range2.color = Color.parseColor("#E3E500")
        range2.from = 10.0
        range2.to = 40.0

        val range3 = Range()
        range3.color = Color.parseColor("#00b20b")
        range3.from = 40.0
        range3.to = 60.0

        fullGaugeCliente.minValue = 0.0
        fullGaugeCliente.maxValue = 60.0

        val myActivity = requireActivity() as MapActivity
        myActivity.contador

        fullGaugeCliente.value = myActivity.contador


        fullGaugeCliente.addRange(range)
        fullGaugeCliente.addRange(range2)
        fullGaugeCliente.addRange(range3)

        fullGaugeCliente.isUseRangeBGColor = true
        fullGaugeCliente.isDisplayValuePoint = false



    }


    // TEMPORORIZADOR DE ESPERA DE RESPUESTA DEL CONDUCTOR********************YO*********
    private fun activartiempo(inicio:Long){
        Log.d("tiempo", "Tiempo fuera del onTick: ${inicio} ")
        countDownTimerbooki = object : CountDownTimer(inicio,1000){
            override fun onTick(millisUntilFinished: Long) {
                Log.d("tiempo", "Tiempo dentro del onTick: ${inicio} ")
                val segundo = (millisUntilFinished/1000).toInt()

                fullGaugeCliente.value= segundo.toDouble()
            }

            override fun onFinish() {



            }

        }.start()
    }
    //configurar la barra de tiempo Gaude ***yo ******************


    //OBTIENE LA INFORMACION DEL CLIENTE
    private fun getClientInfo(idClient: String) {

        progressDialog.showProgressBar(requireActivity())
        clienteProvider.getClientById(idClient).addOnSuccessListener { document ->
            progressDialog.hideProgressBar(requireActivity())
            if (document.exists()) {
                cliente = document.toObject(Client::class.java)
                Log.d("CONDUCTOR", "CONDUCTOR ENCONTRADO EN getDriverInfo: ${Driver} y:  ${cliente?.name}")
                //CARGA LOS DATOS DEL CONDUCTOR A LA BUSQUEDA***********

                Log.d("CONDUCTOR", "CONDUCTOR ENCONTRADO EN datosConductorencontrado: ${cliente?.id} y:  ${cliente?.name}")

                if (cliente?.image != null) {
                    if (cliente?.image != "") {
                        Glide.with(this@ModalBottomSheetBooking).load(cliente?.image).into(circleImageConductor)
                    }
                }
                txtNombreClienteSolicitud.text=cliente?.name.toString()
                //activartiempo()
                //*************************************************

            }
        }

    }



     fun cancelBooking(idClient: String) {
         countDownTimerbooki?.cancel()
        bookingProvider.updateStatus(idClient, "cancel").addOnCompleteListener {
            (activity as MapActivity).musicaMediaPlayerStop()
            (activity as? MapActivity)?.timer?.cancel()
            createHistoryCancel()//CREA HISTORIA DE BOOKING CANCELADOS*******************
            dismiss()
            (activity as? MapActivity)?.disconnectDriver()
            (activity as? MapActivity)?.bookingbandera = null

        }
    }

    private fun acceptBooking(idClient: String) {
        countDownTimerbooki?.cancel()
        bookingProvider.updateStatus(idClient, "accept").addOnCompleteListener {
            (activity as? MapActivity)?.timer?.cancel()
            if (it.isSuccessful) {
                (activity as? MapActivity)?.easyWayLocation?.endUpdates()
                geoProvider.removeLocation(authProvider.getId())
                val activity = requireActivity()
                (activity as MapActivity).musicaMediaPlayerStop()
                (activity as? MapActivity)?.bookingbandera = null

                goToMapTrip()
            }
            else {
                if (context != null) {
                    (activity as MapActivity).musicaMediaPlayerStop()
                    Toast.makeText(activity, "No se pudo aceptar el viaje El cliente Cancelo la Orden: precione Cancelar", Toast.LENGTH_LONG).show()
                }
            }
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

    private fun goToMapTrip() {
        val i = Intent(context, MapTripActivity::class.java)

        val bundle = Bundle()
        bundle.putString("booking", booking.toJson())
        i.putExtras(bundle)
        startActivity(i)

        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context?.startActivity(i)
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (activity as? MapActivity)?.timer?.cancel()
        if (booking.idClient != null) {
           // cancelBooking(booking.idClient!!)
        }

    }

}