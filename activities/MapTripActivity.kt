package com.carlosvicente.uberdriverkotlin.activities

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import androidx.appcompat.app.AppCompatActivity

import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.databinding.ActivityMapTripBinding
import com.carlosvicente.uberdriverkotlin.fragments.ModalBottomSheetBooking
import com.carlosvicente.uberdriverkotlin.fragments.ModalBottomSheetTripInfo
//import com.carlosvicente.uberdriverkotlin.fragments.ModalBottomSheetTripInfo
import com.carlosvicente.uberdriverkotlin.models.*
import com.carlosvicente.uberdriverkotlin.providers.*
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.content.Intent
import android.net.Uri

class MapTripActivity : AppCompatActivity(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack, SensorEventListener {

    var zoom = 0f
    var distanAlPasajero: Location? = null
    private var bearing: Float = 0.0f
    private var totalPrice = 0.0
    private val configProvider = ConfigProvider()
    private var markerDestination: Marker? = null
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var booking: Booking? = null
    private var bookingExtra: Booking? = null
    private var bookingInfo: Booking? = null
    private var bookingActivo: Booking? = null
    private var client: Client? = null
    private var markerOrigin: Marker? = null
    private var bookingListener: ListenerRegistration? = null
    private lateinit var binding: ActivityMapTripBinding
    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()
    private val historyProvider = HistoryProvider()
    private val historyCancelProvider = HistoryCancelProvider()
    private val notificationProvider = NotificationProvider()
    private val clientProvider = ClientProvider()
    private var progressDialog = ProgressDialogFragment

    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    private var isLocationEnabled = false
    private var isCloseToOrigin = false

    // DISTANCIA
    private var meters = 0.0
    private var metersAlPasajero = 0.0
    private var km = 0.0
    private var currentLocation = Location("")
    private var previusLocation = Location("")
    private var isStartedTrip = false

    // MODAL
    private var modalTrip = ModalBottomSheetTripInfo()

    // SENSOR CAMERA
    private var angle = 0
    private val rotationMatrix = FloatArray(16)
    private var sensorManager: SensorManager? = null
    private var vectSensor: Sensor? = null
    private var declination = 0.0f
    private var isFirstTimeOnResume = false
    private var isFirstLocation = false
    private var mapaCargado = false
    private var bookingJson: String? = null





    //PARA EL PRECIO
    private val driverProvider = DriverProvider()
    private var  tipoVehiculo = ""
    var total = 0.0
    private var esNoturno = false

    //AJUSTES DEPURACION
    private var guardando =0

    var seleccion = ""


    // TEMPORIZADOR
    private var counter = 0
    private var min = 0
    private var handler = Handler(Looper.myLooper()!!)
    private var runnable = Runnable {
        kotlin.run {
            counter++

            if (min == 0) {
                binding.textViewTimer.text = "$counter Seg"
            }
            else {
                binding.textViewTimer.text = "$min Min $counter Seg"
            }

            if (counter == 60) {
                min = min + (counter / 60)
                counter = 0
                binding.textViewTimer.text = "$min Min $counter Seg"
            }

            startTimer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        vectSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)

        locationPermissions.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
        mapaCargado = false

        SaberSiesMoto()

        //RECIBIR BOOKING DEL FRAGMENT****************
        val bundle = intent.extras
        if (bundle != null) {
             bookingJson = bundle.getString("booking")
            bookingExtra = Booking.fromJson(bookingJson!!)!!//LO LLEVO A FORMATO JSON
            // Realiza el procesamiento necesario con los datos recibidos
        }

        //********************************************

            binding.btnStartTrip.setOnClickListener {
                binding.btnStartTrip.isEnabled= false
                updateToStarted() }

            binding.btnFinishTrip.setOnClickListener {
            ///evitar precionarlo varia veces
            binding.btnFinishTrip.isEnabled = false
                // Aquí van las acciones que quieres que se realicen después de presionar el botón
            updateToFinish() }
            binding.btnFinishTrip.postDelayed({
            binding.btnFinishTrip.isEnabled = true
                }, 1000) // 1 segundo de espera

            binding.imageViewInfo.setOnClickListener {

                showModalInfo2() }

            binding.floatInfo.setOnClickListener{
                binding.floatInfo.isClickable = false
                binding.imageViewInfo.isClickable = false
                showModalInfo2()}
        binding.btnWaze.setOnClickListener {
            if( binding.btnFinishTrip.visibility == View.GONE){
                abrirWaze(originLatLng!!.latitude,originLatLng!!.longitude)
            }
            if ( binding.btnStartTrip.visibility == View.GONE){
                abrirWaze(destinationLatLng!!.latitude,destinationLatLng!!.longitude)
            }
        }

        binding.switch1.setOnClickListener{startSensor()}


    }

    val locationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when {
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("LOCALIZACION", "Permiso concedido")
                    easyWayLocation?.startLocation()

                }
                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("LOCALIZACION", "Permiso concedido con limitacion")
                    easyWayLocation?.startLocation()
                }
                else -> {
                    Log.d("LOCALIZACION", "Permiso no concedido")
                }
            }
        }

    }
    // Método para abrir Waze y mostrar una ubicación específica
    fun abrirWaze(latitud: Double, longitud: Double) {
        val uri = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud(Waze)")
        val intent = Intent(Intent.ACTION_VIEW, uri)

        // Verifica si hay alguna aplicación de navegación instalada en el dispositivo
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // No hay ninguna aplicación de navegación instalada, muestra un mensaje de error
            Toast.makeText(this, "No se encontró una aplicación de navegación", Toast.LENGTH_SHORT).show()
        }
    }



    //  OBTIENE LA INFORMACION DEL CLIENTE
    private fun getClientInfo() {
        //TRAMPA TEMPORAL PROBLEMAS DEL BOKING VACIO*******YO*******************
        Log.d("BOOKING", "VALOR DEL BOOKING ${bookingActivo?.idClient} bookin viejo: ${booking?.idClient}")
        if (booking?.idClient!= bookingActivo?.idClient) {
            if (booking?.idClient != null) {
                clientProvider.getClientById(booking?.idClient!!).addOnSuccessListener { document ->
                    if (document.exists()) {
                        client = document.toObject(Client::class.java)
                    }
                }
            }
            if (bookingActivo != null) {
                clientProvider.getClientById(bookingActivo?.idClient!!)
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            client = document.toObject(Client::class.java)
                        }
                    }
            }
        }else{
            clientProvider.getClientById(bookingActivo?.idClient!!)
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        client = document.toObject(Client::class.java)
                    }
                }

        }
    }
    //  OBTIENE LA INFORMACION DEL CLIENTE
    private fun getClientInfo2() {
            if (booking?.idClient != null) {
                clientProvider.getClientById(booking?.idClient!!).addOnSuccessListener { document ->
                    if (document.exists()) {
                        client = document.toObject(Client::class.java)
                    }
                }
            }

    }

    // ENVIA NOTIFICACION AL CLIENTE
    private fun sendNotification(status: String) {

        val map = HashMap<String, String>()
        map.put("title", "ESTADO DEL VIAJE")
        map.put("body", status)

        val body = FCMBody(
            to = client?.token!!,
            priority = "high",
            ttl = "4500s",
            data = map
        )

        notificationProvider.sendNotification(body).enqueue(object: Callback<FCMResponse> {
            override fun onResponse(call: Call<FCMResponse>, response: Response<FCMResponse>) {
                if (response.body() != null) {

                    if (response.body()!!.success == 1) {
                        Toast.makeText(this@MapTripActivity, "Se envio la notificacion", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this@MapTripActivity, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show()
                    }

                }
                else {
                    Toast.makeText(this@MapTripActivity, "hubo un error enviando la notificacion", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                Log.d("NOTIFICATION", "ERROR: ${t.message}")
            }

        })
    }

    private fun showModalInfo() {
        Log.d("BOOKING", "BOOKING del showModalInfo  ${bookingInfo?.toJson()} $mapaCargado")
        if (mapaCargado) {
            // El mapa está cargado, realizar operación
            Log.d("BOOKING", "BOOKING del showModalInfo  ${bookingInfo?.toJson()}")
            if (bookingInfo != null) {

                val bundle = Bundle()
                bundle.putString("booking", bookingInfo?.toJson())
                binding.floatInfo.isClickable= true
                binding.imageViewInfo.isClickable = true
                if (supportFragmentManager.findFragmentByTag(ModalBottomSheetTripInfo.TAG) == null) {

                    modalTrip.arguments = bundle
                    modalTrip.show(supportFragmentManager, ModalBottomSheetTripInfo.TAG)
                }


            }
            else {
                progressDialog.hideProgressBar(this)
                binding.floatInfo.isClickable= true
                binding.imageViewInfo.isClickable = true
                Toast.makeText(this, "No se pudo cargar la informacion", Toast.LENGTH_SHORT).show()
            }
        } else {
            // El mapa no está cargado todavía
            Toast.makeText(this, "Porfavor espera a que cargue el mapa", Toast.LENGTH_SHORT).show()
        }


    }

            //correcion de problema
    private fun showModalInfo2() {
        if (mapaCargado) {
            // El mapa está cargado, realizar operación

            if (client != null) {

                val bundle = Bundle()
                bundle.putString("booking", bookingExtra?.toJson())
                binding.floatInfo.isClickable= true
                binding.imageViewInfo.isClickable = true
                if (supportFragmentManager.findFragmentByTag(ModalBottomSheetTripInfo.TAG) == null) {

                    modalTrip.arguments = bundle
                    modalTrip.show(supportFragmentManager, ModalBottomSheetTripInfo.TAG)
                }


            }
            else {
                progressDialog.hideProgressBar(this)
                binding.floatInfo.isClickable= true
                binding.imageViewInfo.isClickable = true
                Toast.makeText(this, "No se pudo cargar la informacion", Toast.LENGTH_SHORT).show()
            }
        } else {
            // El mapa no está cargado todavía
            Toast.makeText(this, "Porfavor espera a que cargue el mapa", Toast.LENGTH_SHORT).show()
        }


    }

    //INICIALIZA EL CONTADOR DEL BOOKING
    private fun startTimer() {
        handler.postDelayed(runnable, 4000) // INICIALIZAR EL CONTADOR
    }

    private fun getDistanceBetween(originLatLng: LatLng, destinationLatLng: LatLng): Float {
        var distance = 0.0f
        val originLocation = Location("")
        val destinationLocation = Location("")

        originLocation.latitude = originLatLng.latitude
        originLocation.longitude = originLatLng.longitude

        destinationLocation.latitude = destinationLatLng.latitude
        destinationLocation.longitude = destinationLatLng.longitude

        distance = originLocation.distanceTo(destinationLocation)
        return distance
    }


    // OBTIENE DEL BOOKING**********************
    private fun getBooking() {
        bookingProvider.getBooking().get().addOnSuccessListener { query ->
            if (query != null) {

                if (query.size() > 0) {
                    booking = query.documents[0].toObject(Booking::class.java)
                    Log.d("FIRESTORE", "BOOKING ${booking?.toJson()}")
                    originLatLng = LatLng(booking?.originLat!!, booking?.originLng!!)
                    destinationLatLng = LatLng(booking?.destinationLat!!, booking?.destinationLng!!)
                    easyDrawRoute(originLatLng!!)
                    addOriginMarker(originLatLng!!)
                    //getClientInfo()
                    getClientInfo2()

                }

            }
        }
    }
    // OBTIENE DEL BOOKING**********************
    private fun getBookingInfo() {
        Log.d("BOOKING", "getBookingInfo afuera del getBookingInfo ${bookingInfo?.toJson()}")
        bookingProvider.getBookingINFO().get().addOnSuccessListener { query ->
            if (query != null) {

                if (query.size() > 0) {
                    bookingInfo = query.documents[0].toObject(Booking::class.java)
                    Log.d("BOOKING", "getBookingInfo ${bookingInfo?.toJson()}")
                }

            }
        }
    }

    //BUSCA SOLO BOOKING ACTIVO ****YO******
    private fun getBookingActivo() {
        bookingProvider.getBookingActivo().get().addOnSuccessListener { query ->
            if (query != null) {

                if (query.size() > 0) {
                    bookingActivo = query.documents[0].toObject(Booking::class.java)
                    Log.d("BOOKING", "BOOKING bookingActivo ${bookingActivo?.toJson()}")
                    originLatLng = LatLng(bookingActivo?.originLat!!, bookingActivo?.originLng!!)
                    destinationLatLng = LatLng(bookingActivo?.destinationLat!!, bookingActivo?.destinationLng!!)
//                    easyDrawRoute(originLatLng!!)
//                    addOriginMarker(originLatLng!!)
                    getClientInfo2()
                }

            }
        }
    }



//DIBUJA LA RUTA
    private fun easyDrawRoute(position: LatLng) {
        wayPoints.clear()
        wayPoints.add(myLocationLatLng!!)
        wayPoints.add(position)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(myLocationLatLng!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.green)

            .setPolyLineWidth(14)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(position)
            .build()

        directionUtil.initPath()
    }

    private fun addOriginMarker(position: LatLng) {
        markerOrigin = googleMap?.addMarker(MarkerOptions().position(position).title("Recoger aqui")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }

    private fun addDestinationMarker() {
        if (destinationLatLng != null) {
            markerDestination = googleMap?.addMarker(MarkerOptions().position(destinationLatLng!!).title("Recoger aqui")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin)))
        }
    }

//GUARDA LA POSICION EN TIEMPO REAL QUE SALE DE CURRIENT LOCATION***
    private fun  saveLocation() {
        if (myLocationLatLng != null) {
            geoProvider.saveLocationWorking(authProvider.getId(), myLocationLatLng!!)
        }
    }

    private fun disconnectDriver() {
        easyWayLocation?.endUpdates()
        if (myLocationLatLng != null) {
            geoProvider.removeLocation(authProvider.getId())
        }
    }

    private fun showButtonFinish() {
        binding.btnStartTrip.visibility = View.GONE
        binding.btnFinishTrip.visibility = View.VISIBLE
    }

    private fun addMarker() {
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.uber_car)
        val markerIcon = getMarkerFromDrawable(drawable!!)
        if (markerDriver != null) {
            markerDriver?.remove() // NO REDIBUJAR EL ICONO
        }
        if (myLocationLatLng != null) {
            markerDriver = googleMap?.addMarker(
                MarkerOptions()
                    .position(myLocationLatLng!!)
                    .anchor(0.5f, 0.5f)
                    .flat(true)
                    .icon(markerIcon)
            )
        }
    }



    override fun onDestroy() { // CIERRA APLICACION O PASAMOS A OTRA ACTIVITY
        super.onDestroy()
        easyWayLocation?.endUpdates()
        handler.removeCallbacks(runnable)
        stopSensor()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        startSensor()
//        easyWayLocation?.startLocation();

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        googleMap?.isMyLocationEnabled = false

        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style)
            )
            if (!success!!) {
                Log.d("MAPAS", "No se pudo encontrar el estilo")
            }

        } catch (e: Resources.NotFoundException) {
            Log.d("MAPAS", "Error: ${e.toString()}")
        }
        mapaCargado = true

    }


/// CAMBIA ESTADO DEL BOOKING A STARTED ***********************
    private fun updateToStarted() {
        //PARA ELIMINAR LA CERCANIA CON EL PASAJERO
        isCloseToOrigin= true
        //*****************************************************
        if (isCloseToOrigin) {

            bookingProvider.updateStatus(bookingExtra?.idClient!!, "started").addOnCompleteListener {
                Log.d("HISTORIA", "VALOR DE BOOKING.IDCLIENT y BOOKING.STATUS ${bookingExtra?.idClient}  ${booking?.status}  ")
                if (it.isSuccessful) {
                    if (destinationLatLng != null) {
                        isStartedTrip = true
                        googleMap?.clear()
                        addDirectionMarker(myLocationLatLng!!, angle)
                        easyDrawRoute(destinationLatLng!!)
                        markerOrigin?.remove()
                        addDestinationMarker()
                        startTimer()
                        sendNotification("Viaje iniciado")
                    }

                    showButtonFinish()
                }
                progressDialog.hideProgressBar(this)
            }
        }
        else {
        //    progressDialog.hideProgressBar(this)
            Log.d("LOCATION", "DISTANCIA ES MAYOR  ")
            binding.btnStartTrip.isEnabled= true
            Toast.makeText(this, "Debes estar mas cerca a la posicion de recogida", Toast.LENGTH_LONG).show()
       //     progressDialog.hideProgressBar(this)
        }
    }

    private fun updateToFinish() {
        Log.d("INAVILITAR", "EL VALOR DE GUARDANDO $guardando ")

           // binding.btnFinishTrip.isEnabled = true   // INABILITA EL BOTON FINISH

            handler.removeCallbacks(runnable) // DETENER CONTADOR
            isStartedTrip = false
            easyWayLocation?.endUpdates()
            geoProvider.removeLocationWorking(authProvider.getId())
            if (min == 0) {
                min = 1
            }
            total = bookingExtra?.price!!
            createHistory()
           // getPrices(km, min.toDouble())
             cambiaEstado()



    }

    private fun createHistory() {
        Log.d("HISTORIA", "HISTORIA booking viejo ${booking} BOOKING ACTIVO: ${bookingActivo} ")
        val history = History(
            idDriver = authProvider.getId(),
            idClient = bookingExtra?.idClient,
            origin = bookingExtra?.origin,
            destination = bookingExtra?.destination,
            originLat = bookingExtra?.originLat,
            originLng = bookingExtra?.originLng,
            destinationLat = bookingExtra?.destinationLat,
            destinationLng = bookingExtra?.destinationLng,
            time = min,
            km = km,
            price = bookingExtra?.price,
            timestamp = Date().time,
            date = bookingExtra?.date
            
        )
        historyProvider.create(history).addOnCompleteListener {
            if (it.isSuccessful) {
                guardando = 0
               // binding.btnFinishTrip.isEnabled = false//HABILITA EL BOTON DESPUES DE GUARDAR
               // Toast.makeText(this, "Historia creada Satistactorimente", Toast.LENGTH_LONG).show()


            }
        }

    }


    private fun cambiaEstado(){

        bookingProvider.updateStatus(bookingExtra?.idClient!!, "finished").addOnCompleteListener {
            if (it.isSuccessful) {
                guardando = 0
                sendNotification("Viaje terminado")
                goToCalificationClient()// LLAMA AL ACTIVITY CALIFICACIONES
            }
        }
        bookingProvider.updateActivo (bookingExtra?.idClient!!, false).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("PRICE", "CAMBIO EL ESTADO DE ACTIVO A FALSE")
            }
        }

    }

    //OPTIENE EL PRECIO DE VIAJE!(YO)**********
    private fun getPrices(distance: Double, time: Double) {
        var CcortaMoto = 0.0
        var CmediaMoto = 0.0
        var ClargaMoto = 0.0
        var CcortaCarro = 0.0
        var CmediaCarro = 0.0
        var ClargaCarro = 0.0
        var kmCarro = 0.0
        var kmMoto = 0.0
        progressDialog.showProgressBar(this)
        configProvider.getPrices().addOnSuccessListener { document ->
            progressDialog.hideProgressBar(this)
            Log.d("PRICE", "VALOR DE Document $document ")
            //** verifica si es horario nocturno*************
            val horaActual = LocalTime.now()
            val horaLimite1 = LocalTime.of(22, 0) // 11:00 PM
            val horaLimite2 = LocalTime.of(6, 0) // 6:00 AM

            if (horaActual.isAfter(horaLimite1) || horaActual.isBefore(horaLimite2)) {
                esNoturno=true
            } else {
                esNoturno= false
            }
            //***********************************************

            if (document.exists()) {
                val prices = document.toObject(Prices::class.java) // DOCUMENTO CON LA INFORMACION
                if (esNoturno){
                    CcortaMoto = prices?.CcortaMoto!!.toDouble()*1.5
                    CmediaMoto = prices?.CmediaMoto!!.toDouble()*1.5
                    ClargaMoto = prices?.CLargaMoto!!.toDouble()*1.5
                    CcortaCarro = prices?.CcortaCarro!!.toDouble()*1.5
                    CmediaCarro = prices?.CMediaCarro!!.toDouble()*1.5
                    ClargaCarro = prices?.CLargaCarro!!.toDouble()*1.5
                    kmCarro = prices?.kmCarro!!.toDouble()*1.5
                    kmMoto = prices?.kmMoto!!.toDouble()*1.5
                }else{

                }
                if (!esNoturno){
                    CcortaMoto = prices?.CcortaMoto!!.toDouble()
                    CmediaMoto = prices?.CmediaMoto!!.toDouble()
                    ClargaMoto = prices?.CLargaMoto!!.toDouble()
                    CcortaCarro = prices?.CcortaCarro!!.toDouble()
                    CmediaCarro = prices?.CMediaCarro!!.toDouble()
                    ClargaCarro = prices?.CLargaCarro!!.toDouble()
                    kmCarro = prices?.kmCarro!!.toDouble()
                    kmMoto = prices?.kmMoto!!.toDouble()
                }


                if (tipoVehiculo == "Carro"){

                    if (distance<3) {
                        total = CcortaCarro!!.toDouble()
                    }
                    if (distance>3 && distance<5){
                        total = CmediaCarro!!.toDouble()
                    }
                    if (distance>5 && distance<7){

                        total = ClargaCarro!!.toDouble()
                    }
                    if (distance>7){ // FALTA CALCULAR BIEN DESPUES DE 12KM
                        total= ClargaCarro!!.toDouble()+ (distance-7)*kmCarro!!.toDouble()
                    }
                }

                if (tipoVehiculo == "Moto"){
                    if (distance<5) {
                        total = CcortaMoto!!.toDouble()
                    }
                    if (distance>5 && distance<12){
                        total = CmediaMoto!!.toDouble()
                    }
                    if (distance>12){ // FALTA CALCULAR BIEN DESPUES DE 12KM
                        total = distance*kmMoto!!.toDouble()
                    }

                }

                Log.d("PRICE", "VALOR DE TOTAL FUERA $total ")
                val totalDosDeci = String.format("%.1f", total)
                //  val maxTotalString = String.format("%.1f", maxTotal)
                createHistory()
                cambiaEstado()
            }

        }
    }





    private fun goToCalificationClient() {
        val i = Intent(this, CalificationClientActivity::class.java)
        i.putExtra("price", total)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location) { // ACTUALIZACION DE LA POSICION EN TIEMPO REAL
        myLocationLatLng = LatLng(location.latitude, location.longitude) // LAT Y LONG DE LA POSICION ACTUAL
        currentLocation = location
        Log.d("LOCATION", "Distance: ${currentLocation}")
        if (isStartedTrip) {
            meters = meters + previusLocation.distanceTo(currentLocation)
            km = meters / 1000
            binding.textViewDistance.text = "${String.format("%.1f", km)} km"
        }

        previusLocation = location

//        if (!isFirstLocation) {
//            isFirstLocation = true
//            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
//                CameraPosition.builder().target(myLocationLatLng!!).zoom(19f).build()
//            ))
//        }

        Log.d("DISTANCIA", "currentLocation: $metersAlPasajero y zomm $zoom")
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(myLocationLatLng!!).bearing(bearing).tilt(50f).zoom(14f).build()//ZOM DE LA CAMARA
        ))
        addDirectionMarker(myLocationLatLng!!, angle)
        saveLocation()

        if (booking != null && originLatLng != null) {
            var distance = getDistanceBetween(myLocationLatLng!!, originLatLng!!)
            if (distance <= 1030) { //PARA DETERMINAR A CUANTOS ,METROS DEBE D ESTAR DEL CLIENTE
                isCloseToOrigin = true
            }
            Log.d("LOCATION", "Distance: ${distance}")
        }

        if (!isLocationEnabled) {
            isLocationEnabled = true
            getBooking()
            //getBookingActivo()

        }

    }
    private fun asiganarlocation(){

        originLatLng.toString()
// Supongamos que tenemos una variable latlong que contiene la latitud y longitud:
        val latlong = originLatLng.toString()

// Separamos la latitud y longitud en dos variables distintas:
        val (lat, long) = latlong.split(",").map { it.toDouble() }

// Creamos una instancia de la clase Location y le asignamos los valores de latitud y longitud:
        distanAlPasajero= Location("").apply {
            setLatitude(lat)
            setLongitude(long)
        }
    }

    override fun locationCancelled() {

    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        directionUtil.drawPath(WAY_POINT_TAG)
    }

    private fun updateCamera(bearing: Float) {
        val oldPos = googleMap?.cameraPosition
        val pos = CameraPosition.builder(oldPos!!).bearing(bearing).tilt(50f).build()
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
        if (myLocationLatLng != null) {
            addDirectionMarker(myLocationLatLng!!, angle)
        }
    }

    //COLOCA EL ICONO DE CIRCULO DE FLECHA////
    private fun addDirectionMarker(latLng: LatLng, angle: Int)  {
        val circleDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_up_arrow_circle)
        val markerIcon = getMarkerFromDrawable(circleDrawable!!)
        if (markerDriver != null) {
            markerDriver?.remove()
        }
        markerDriver = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 0.5f)
                .rotation(angle.toFloat())
                .flat(true)
                .icon(markerIcon)
        )
    }

    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            120,
            120,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,120,120)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            if (Math.abs(Math.toDegrees(orientation[0].toDouble()) - angle) > 0.8 ) {
                bearing = Math.toDegrees(orientation[0].toDouble()).toFloat() + declination
                updateCamera(bearing)
            }
            angle = Math.toDegrees(orientation[0].toDouble()).toInt()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    //FUNCION PARA ACTUALIZAR EL SENSOR SI EL SWITCHE ESTA PASADO********
    private fun startSensor() {
       if (binding.switch1.isChecked){

                // lógica cuando el Switch está encendido
                if (sensorManager != null) {
                    sensorManager?.registerListener(this, vectSensor, SensorManager.SENSOR_STATUS_ACCURACY_LOW)
                }
                   // Toast.makeText(this, "Sensor de Movimiento Activado", Toast.LENGTH_SHORT).show()
                     Log.d("Switch", "Encendido")
                } else {
                    // lógica cuando el Switch está apagado
              // Toast.makeText(this, "Sensor de Movimiento Desactivado", Toast.LENGTH_SHORT).show()
                    stopSensor()
                    Log.d("Switch", "Apagado")
                }
    }




    private fun stopSensor () {
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume() // ABRIMOS LA PANTALLA ACTUAL
        if (!isFirstTimeOnResume) {
            isFirstTimeOnResume = true
        }
        else {
            startSensor()
        }
    }

    override fun onPause() {
        super.onPause()
        stopSensor()
    }
    //MODIFICACION MIA**********

    // VERIFICA SI ES CARRO O MOTO YO************************
    private fun SaberSiesMoto(){
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()) {
                val driver = document.toObject(Driver::class.java)


                if (driver?.tipo.toString() == "Carro"){
                    tipoVehiculo = "Carro"
                    Log.d("LOCALIZACION", "ENTRO A CARRO: tipoVehiculo  $tipoVehiculo  ${driver!!.tipo}")
                }
                if (driver?.tipo.toString()=="Moto"){
                    tipoVehiculo = "Moto"
                    Log.d("LOCALIZACION", "ENTRO A MOTO: tipoVehiculo  $tipoVehiculo  ${driver?.tipo}")
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
            fecha = Date(),
            causa = "Cancelado por Conductor Despues de Iniciar Viaje",
            causaConductor = seleccion


        )
        historyCancelProvider.create(historyCancel).addOnCompleteListener {
            if (it.isSuccessful) {

                Log.d("HISTOCANCEL", "LA HISTORIA DE CANCEL $historyCancel ")

            }
        }
    }

    fun cancelBooking(idClient: String) {
        bookingProvider.updateStatus(idClient, "cancel").addOnCompleteListener {
            val fragmentManager = supportFragmentManager
            val modalBottomSheet = ModalBottomSheetBooking()
            val fragmentTransaction = fragmentManager.beginTransaction()

            createHistoryCancel()//CREA HISTORIA DE BOOKING CANCELADOS*******************

        }
    }

    //MENSAGE DE CONFIRMACION DE SALIDA*********************
    fun salirdelViaje(){

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Salir")
        builder.setMessage("Desea salir y cancelar la Carrera?")
        builder.setPositiveButton("Salir", DialogInterface.OnClickListener { dialog, which ->
           // cancelardespuesInicio()
            cancelBooking(client?.id.toString())

            goToMap()
        })
        builder.setNegativeButton("Cancelar",null )
        builder.show()

    }

    private fun goToMap() {
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }
//    private fun cancelardespuesInicio(){
//
//        val bundle = bundleOf(NAME_BUNDLE to "Aristidef",
//            ADDRESS_BUNDLE to "Direccion Mi casa")
//            supportFragmentManager.commit {
//            setReorderingAllowed(true)
//            add<ModalBottomCancelCarrera>(R.id.fragmenCancelVIniciado , args = bundle)
//        }
//    }
    override fun onStart() {
        super.onStart()
    getBookingInfo()
    }


    override fun onBackPressed() {
        porqueCancelo()
        // Aquí puedes colocar el código para manejar la acción del botón "Atrás"
        // Por ejemplo, puedes finalizar la actividad actual:
        //salirdelViaje()
    }

    private fun porqueCancelo() {
        val opciones = arrayOf("No consigui el Pasajero", "Fue un Viaje de Prueba", "No se Pudo contactar al pasajero y preferi No ir","otro")
        val singleChoiceDialogo = AlertDialog.Builder(this)
            .setTitle("Cancelar Carrera")

            .setSingleChoiceItems(opciones, 0) { dialog, which ->

                // Aquí puedes hacer lo que necesites con la opción seleccionada
                // Por ejemplo, mostrarla en un Toast:
                Toast.makeText(this, "Seleccionaste la opción ${opciones[which]}", Toast.LENGTH_SHORT).show()
                seleccion = opciones[which]
            }
            .setPositiveButton("Aceptar"){_,_ ->
                Toast.makeText(this, "Seleccionaste aceptar $seleccion", Toast.LENGTH_LONG).show()

                cancelBooking(client?.id.toString())

                goToMap()
            }
            .setNegativeButton("Cancelar"){_,_ ->
                return@setNegativeButton
                Toast.makeText(this, "Seleccionaste Cancelar", Toast.LENGTH_SHORT).show()
            }
            .create()

            singleChoiceDialogo.show()
        }





}