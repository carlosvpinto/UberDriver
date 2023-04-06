package com.carlosvicente.uberdriverkotlin.activities


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.*
import android.location.Location
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.*
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration
import com.carlosvicente.uberdriverkotlin.R
import com.carlosvicente.uberdriverkotlin.databinding.ActivityMapBinding
import com.carlosvicente.uberdriverkotlin.fragments.FragmenRecibir
import com.carlosvicente.uberdriverkotlin.fragments.ModalBottomSheetBooking
import com.carlosvicente.uberdriverkotlin.fragments.ModalBottomSheetMenu
import com.carlosvicente.uberdriverkotlin.models.Booking
import com.carlosvicente.uberdriverkotlin.models.Driver
import com.carlosvicente.uberdriverkotlin.models.HistoryDriverCancel
import com.carlosvicente.uberdriverkotlin.providers.*
import com.carlosvicente.uberdriverkotlin.services.service
//import com.carlosvicente.uberdriverkotlin.services.servicios
import kotlinx.android.synthetic.main.modal_bottom_sheet_booking.*

import java.util.*



class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener, SensorEventListener {

    private var bearing: Float = 0.0f
    private var bookingListener: ListenerRegistration? = null
    private val bookingProvider = BookingProvider()
    private lateinit var binding: ActivityMapBinding
    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()


    private val driverProvider = DriverProvider()
    private val notificationProvider = NotificationProvider()
    private val modalBooking = ModalBottomSheetBooking()

    private val modalRecibir = FragmenRecibir()
    private val modalMenu = ModalBottomSheetMenu()

    private var driver: Driver? = null
    //PARA EL SONIDO
    private lateinit var SP: SoundPool
    private  var sonido_alart: Int = 0
    private lateinit var Mp : MediaPlayer
    private var mediaPlayer: MediaPlayer? = null



    // SENSOR CAMERA
    private var angle = 0
    private val rotationMatrix = FloatArray(16)
    private var sensorManager: SensorManager? = null
    private var vectSensor: Sensor? = null
    private var declination = 0.0f
    private var isFirstTimeOnResume = false
    private var isFirstLocation = false



    //Moto
    val origin: String? = null
    private var isMotoTrip = true

    var autorizadoCondu = false
    var disponibleDriver = true

    //PARA ELIMINAR SALIENDO
    var banderaActiva: Boolean = false
    var bookingbandera: Booking? = null
    var bookingReserva: Booking? = null
    private val historyCancelProvider = HistoryCancelProvider()
    var countDownTimerViaje : CountDownTimer? = null

    var countDownTimer: CountDownTimer? = null
    var contador= 0.0



    val timer = object: CountDownTimer(60000, 1000) {
        override fun onTick(counter: Long) {
            contador = (counter/ 1000).toDouble()
            // Obtener una referencia al Fragment


            Log.d("TIMER", "Counter: $counter")
        }

        override fun onFinish() {
            Log.d("TIMER", "ON FINISH")
    if (modalBooking.isAdded){
        modalBooking.dismiss()
    }


        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
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



        //PARA SOLICITAR PERMISO DE SUPERPOSICION***********
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(intent)
        }
        //**************************************************

        //PARA SOLICITAR PERMISO DE SUPERPOSICION***********
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
//            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
//            startActivityForResult(intent, REQUEST_CODE_DRAW_OVERLAY_PERMISSION)
//        } else {
//            // El permiso ya se ha otorgado
//            showIncomingCallScreen()
//        }
        //**************************************************

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        vectSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)

        locationPermissions.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))



        SaberSiesMoto() // VERIFICA SI ES MOTO YO***********
        verificaActivacion()// REALIZA ACTIVACION DE LA APLICACION YO **************
        listenerBooking()
        createToken()
        connectDriver()// CONECTA ENTRANDO



        binding.btnConnect.setOnClickListener { connectDriver() }
        binding.btnDisconnect.setOnClickListener { disconnectDriver() }
        binding.imageViewMenu.setOnClickListener { showModalMenu() }
        binding.imageViewSalir.setOnClickListener{salirdelApp()}
        binding.switch1.setOnClickListener{startSensor()}




    }

    private fun llamaAlService() {
        val intent = Intent(this, service::class.java)
        startService(intent)

    }


    val locationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when {
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("LOCALIZACION", "Permiso concedido")
//                    easyWayLocation?.startLocation();
                 //  checkIfDriverIsConnected()
                }
                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("LOCALIZACION", "Permiso concedido con limitacion")
//                    easyWayLocation?.startLocation();
                   // checkIfDriverIsConnected()
                }
                else -> {
                    Log.d("LOCALIZACION", "Permiso no concedido")
                }
            }
        }

    }

    //ACTIVA EL TIEMPO PARA EL TEMPORIZADOR********YO******************************
     fun activartiempo(){
        countDownTimerViaje = object : CountDownTimer(60000,1000){
            override fun onTick(millisUntilFinished: Long) {
                val segundo = (millisUntilFinished/1000).toInt()

            }

            override fun onFinish() {

            }

        }.start()
    }


    // ESCUCHA EL BOOKING SI EL ESTADO ES CREATE******************************************************
    fun listenerBooking() {
        bookingListener = bookingProvider.getBooking().addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d("FIRESTORE", "ERROR: ${e.message}")
                return@addSnapshotListener
            }
            Log.d("ESCUCHANDO", "ERROR: ${snapshot?.documents?.size}")
            if (snapshot != null) {
                var CantBook = 0
                CantBook = snapshot.documents.size
                var Contador = 0
                if (snapshot.documents.size > 0) {
                    while (Contador < CantBook){

                        val booking = snapshot.documents[Contador].toObject(Booking::class.java)
                        Contador++
                        if (booking?.status == "create"){
                            bookingbandera = booking
                            bookingReserva = booking

                            //verica si esta activa la actividad
                            musicaMediaPlayer()

                            val fragmentTag = "ModalBottomSheet"
                            val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
                            if (existingFragment == null) {
                                // El fragmento no existe, se puede agregar

                                showModalBooking(booking!!)
                            } else {
                                // El fragmento ya existe, no se debe agregar nuevamente
                            }

                        }

                    }

                }
            }

        }
    }
    private fun llamaService(){
        val intent = Intent(this, service::class.java)
        startService(intent)


    }


    fun cancelBooking(idClient: String) {
        bookingProvider.updateStatus(idClient, "cancel").addOnCompleteListener {
            val fragmentManager = supportFragmentManager
            val modalBottomSheet = ModalBottomSheetBooking()
            val fragmentTransaction = fragmentManager.beginTransaction()
            musicaMediaPlayerStop()
            bookingbandera=null
            createHistoryCancel()//CREA HISTORIA DE BOOKING CANCELADOS*******************

        }
    }
    //CREA HISTORIA DE BOOKING CANCELADOS!!!!**************************
    private fun createHistoryCancel() {

        Log.d("PRICE", "VALOR DE TOTAL  ")
        val historyCancel = HistoryDriverCancel(
            idDriver = authProvider.getId(),
            idClient = bookingReserva?.idClient,
            origin = bookingReserva?.origin,
            destination = bookingReserva?.destination,
            originLat = bookingReserva?.originLat,
            originLng = bookingReserva?.originLng,
            destinationLat = bookingReserva?.destinationLat,
            destinationLng = bookingReserva?.destinationLng,
            timestamp = Date().time
        )
        historyCancelProvider.create(historyCancel).addOnCompleteListener {
            if (it.isSuccessful) {

                Log.d("HISTOCANCEL", "LA HISTORIA DE CANCEL $historyCancel ")

            }
        }
    }


//para saber si la actyivity esta activa
//    private fun isAppInForeground(): Boolean {
//        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val appProcesses = activityManager.runningAppProcesses ?: return false
//        for (appProcess in appProcesses) {
//            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
//                return true
//            }
//        }
//        return false
//    }


//FINALIZA AL TONO EN SEGUNDO PLANO ***
fun musicaMediaPlayerStop(){
//        val intent = Intent(this, servicios::class.java)
//        stopService(intent)

         mediaPlayer?.stop()
    }

    //LLAMA AL TONO EN SEGUNDO PLANO ***
    fun musicaMediaPlayer(){

        timer.start()
        mediaPlayer = MediaPlayer.create(this, R.raw.samsungtono)
        mediaPlayer?.start()
//            val serviceIntent = Intent(this, servicios::class.java)
//        startActivity(serviceIntent)

    }



    //MODIFICA LA DISPONIBILIDAD DEL CONDUCTOR
    private fun disponibilidadTrue(){
        if (authProvider.getId()!= ""){
            driverProvider.updateDisponible(authProvider.getId(), true).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("ACTIVANDO", "Entro actualizar disponibilidad DISPONIBLE ")
                }
            }
            showButtonDisconnect()
        }

    }
    private fun disponibilidadFalse(){
        if (authProvider.getId()!= "") {
            driverProvider.updateDisponible(authProvider.getId(), false).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("ACTIVANDO", "Entro actualizar disponibilidad FALSA ")
                }
            }
            showButtonConnect()
        }
    }





//LLAMA EL FRAGMENT**********************************************************************
    private fun showModalBooking(booking: Booking) {

    if (banderaActiva!= true) {
        val bundle = Bundle()
        bundle.putString("booking", booking.toJson())
        modalBooking.arguments = bundle
        modalBooking.isCancelable = false // NO PUEDA OCULTAR EL MODAL BOTTTOM SHEET

        modalBooking.show(supportFragmentManager, ModalBottomSheetBooking.TAG)

        //timer.start()
    } else {
        // La actividad no está activa
    }

    }




    class RideRequestReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val rideRequestIntent = Intent(context, MapActivity::class.java)
            rideRequestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(rideRequestIntent)
        }
    }

    //LLAMA AL ACTIVITID DE NAVEGACION goToMapTrip ***YO******
    private fun goToMapTrip() {
        val i = Intent(this, MapTripActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    //VERIFICA SI EL CONDUCTOR ESTA CONECTADO
    private fun checkIfDriverIsConnected() {
        if (isMotoTrip == true){
            geoProvider.getLocatioMoto(authProvider.getId()).addOnSuccessListener { document ->
                if (document.exists()) {
                    if (document.contains("l")) {
                        connectDriver()
                    } else {
                        showButtonConnect()
                    }
                } else {
                    showButtonConnect()
                }
            }

        }

        if (isMotoTrip!= true) {
            geoProvider.getLocation(authProvider.getId()).addOnSuccessListener { document ->
                if (document.exists()) {
                    if (document.contains("l")) {
                        connectDriver()
                    } else {
                        showButtonConnect()
                    }
                } else {
                    showButtonConnect()
                }
            }
        }
    }

    // GUARDA LA POSICION DEL VEHICULO
    private fun saveLocation() {
        Log.d("MOVIMIENTO", "MOVIMIENTO VEHICULO FUERA DEL IF:: $myLocationLatLng")
        if (authProvider.getId()!= "") {
            Log.d("MOVIMIENTO", "MOVIMIENTO VEHICULO DENTRO DEL IF:: $myLocationLatLng")
            if (myLocationLatLng != null && authProvider.getId() != null) {
                if (authProvider.getId() != "") {
                    geoProvider.saveLocation(authProvider.getId(), myLocationLatLng!!)
                }
            }
        }
    }
    // METODO GUARDAR POSICION DE MOTO
    private fun saveLocationMoto(){
        if (authProvider.getId()!= "") {
            if (myLocationLatLng != null) {
                geoProvider.saveLocationMoto(authProvider.getId(), myLocationLatLng!!)
            }
        }
    }

    //DESCONECTA LA MOTO O EL CARRO YO **********************************
    fun disconnectDriver() {
        easyWayLocation?.endUpdates()
        if (authProvider.getId()!= "") {
            disponibilidadFalse()
            easyWayLocation?.endUpdates()

            if (isMotoTrip== false){
                if (myLocationLatLng != null) {
                    geoProvider.removeLocation(authProvider.getId())
                    showButtonConnect()
                }
            }


            // DESCONECTAR MOTO
            if (isMotoTrip== true){
                if (myLocationLatLng != null) {
                    geoProvider.removeLocationMoto(authProvider.getId())
                    showButtonConnect()
                }
            }

        }
    }

    private fun connectDriver() {
        disponibilidadTrue()
        easyWayLocation?.endUpdates() // OTROS HILOS DE EJECUCION
        easyWayLocation?.startLocation()
        Log.d("LOCALIZAR", "VALOR starLocation: $easyWayLocation")
        showButtonDisconnect()

    }

    private fun showButtonConnect() {
        binding.btnDisconnect.visibility = View.GONE // OCULTANDO EL BOTON DE DESCONECTARSE
        binding.btnConnect.visibility = View.VISIBLE // MOSTRANDO EL BOTON DE CONECTARSE
        binding.imgConectado.visibility = View.GONE
        binding.imgDesconectado.visibility = View.VISIBLE
    }


    private fun showButtonDisconnect() {
        binding.btnDisconnect.visibility = View.VISIBLE // MOSTRANDO EL BOTON DE DESCONECTARSE
        binding.btnConnect.visibility = View.GONE // OCULTANDO EL BOTON DE CONECTARSE
        binding.imgConectado.visibility = View.VISIBLE
        binding.imgDesconectado.visibility = View.GONE
    }




    // VERIFICA SI ES CARRO O MOTO YO************************
    private fun SaberSiesMoto(){
        if (authProvider.getId()!= "") {
            driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
                if (document.exists()) {
                    val driver = document.toObject(Driver::class.java)
                    autorizadoCondu = driver?.activado!!
                    disponibleDriver = driver?.disponible!!
                    Log.d("LOCALIZACION",
                        "Private Saber si es moto: autorizadoCondu  $autorizadoCondu  ${driver.tipo}")

                    if (driver?.tipo.toString() == "Carro") {
                        isMotoTrip = false
                        Log.d("LOCALIZACION",
                            "ENTRO A CARRO: autorizadoCondu  $autorizadoCondu  ${driver.tipo}")
                    }
                    if (driver?.tipo.toString() == "Moto") {
                        isMotoTrip = true
                        Log.d("LOCALIZACION",
                            "ENTRO A MOTO: autorizadoCondu  $autorizadoCondu  ${driver.tipo}")
                    }


                }
            }
        }
    }

    private fun createToken() {
        driverProvider.createToken(authProvider.getId())
    }

    private fun showModalMenu() {
        modalMenu.show(supportFragmentManager, ModalBottomSheetMenu.TAG)
    }
    //MENSAGE DE CONFIRMACION DE SALIDA*********************
    fun salirdelApp(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Salir")
        builder.setMessage("Desea salir de la app")
        builder.setPositiveButton("Salir", DialogInterface.OnClickListener { dialog, which ->
            easyWayLocation?.endUpdates()
            finishAffinity()
        })
        builder.setNegativeButton("Cancelar",null )
        builder.show()
    }
    //VERIFICA SI ESTA ACTIVO EL USUARIO YO***********************
    private fun verificaActivacion(){
        if (authProvider.getId()!= "") {
            driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
                Log.d("ACTIVANDO", "VERIFICANDO EL DOCUMENTO $document")
                if (document.exists()) {
                    val driver = document.toObject(Driver::class.java)
                    autorizadoCondu = driver?.activado!!
                    Log.d("ACTIVANDO", "Entro a desactivar autorizadoCondu $autorizadoCondu ")
                    if (autorizadoCondu == false) {
                        disconnectDriver()
                        // showButtonConnect()
                        binding.btnConnect.isEnabled = false
                        binding.txtActivar2.visibility =
                            View.VISIBLE // MOSTRANDO EL TEXTO DE ACTIVAR
                        binding.txtActivar1.visibility =
                            View.VISIBLE //MOSTRANDO EL TEXTO DE ACTIVAR
                    }
                    if (autorizadoCondu == true) {
                        connectDriver()
                        showButtonDisconnect()

                        binding.btnConnect.isEnabled = true
                        Log.d("ACTIVANDO", "Entro a activavar $autorizadoCondu ")
                        binding.txtActivar2.visibility = View.GONE // MOSTRANDO EL TEXTO DE ACTIVAR
                        binding.txtActivar1.visibility = View.GONE //MOSTRANDO EL TEXTO DE ACTIVAR
                    }
                }
            }
        }
    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

//        easyWayLocation?.startLocation();
        startSensor()
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

    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location) { // ACTUALIZACION DE LA POSICION EN TIEMPO REAL
        myLocationLatLng = LatLng(location.latitude, location.longitude) // LAT Y LONG DE LA POSICION ACTUAL


        val field = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        )
        declination = field.declination

                if (!isFirstLocation) {
                    googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder().target(myLocationLatLng!!).bearing(bearing).tilt(50f).zoom(13f).build()
                    ))
                    addDirectionMarker(myLocationLatLng!!, angle)
                    isFirstLocation = true

                    //VERIFICA SI ES MOTO O CARRO/////
                    SaberSiesMoto()
                    connectDriver()
                }
        if (disponibleDriver==true){
            if (isMotoTrip== true){
                if (autorizadoCondu == true){

                    saveLocationMoto()
                    //connectDriver()
                }
            }
            if (isMotoTrip== false){
                if (autorizadoCondu == true){
                    saveLocation()
                   // connectDriver()
                }
            }

        }

    }

    override fun locationCancelled() {
        Toast.makeText(this, "Location Cancelled", Toast.LENGTH_SHORT).show();
    }


    private fun updateCamera(bearing: Float) {
        val oldPos = googleMap?.cameraPosition
        val pos = CameraPosition.builder(oldPos!!).bearing(bearing).tilt(50f).build()
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
        if (myLocationLatLng != null) {
            addDirectionMarker(myLocationLatLng!!, angle)
        }
    }

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
            //Toast.makeText(this, "Sensor de Movimiento Desactivado", Toast.LENGTH_SHORT).show()
            Log.d("Switch", "Apagado")
            stopSensor()
        }
    }

    private fun stopSensor () {
        sensorManager?.unregisterListener(this)
    }
    override fun onStart() {
        super.onStart()
       // listenerBooking()
       // Toast.makeText(this, "OnStart", Toast.LENGTH_SHORT).show()
        // La actividad est� a punto de hacerse visible.
    }

    override fun onResume() {
        super.onResume() // ABRIMOS LA PANTALLA ACTUAL

            banderaActiva= false
        if (bookingbandera!= null){
            // verifica que no se alla llamado el fragmen******************************
            val fragmentTag = "ModalBottomSheet"
            val existingFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
            if (existingFragment == null) {
                // El fragmento no existe, se puede agregar

                showModalBooking(bookingbandera!!)
                bookingbandera= null//limpia el booking coreccion2
            } else {
                // El fragmento ya existe, no se debe agregar nuevamente
            }//***********************************************************************

           // showModalBooking(bookingbandera!!)
            bookingbandera= null// limpia el booking
        }

        if (!isFirstTimeOnResume) {
            isFirstTimeOnResume = true
        }
        else {
            startSensor()
        }
    }

    override fun onPause() {
        super.onPause()
        //Toast.makeText(this, "OnPause", Toast.LENGTH_SHORT).show()

            banderaActiva= true


        stopSensor()
    }


    override fun onStop() {
        super.onStop()
        //Toast.makeText(this, "OnStop", Toast.LENGTH_SHORT).show()
        // La actividad ya no es visible (ahora est� "detenida")
    }
    override fun onDestroy() { // CIERRA APLICACION O PASAMOS A OTRA ACTIVITY
        salirEliminando()

        super.onDestroy()
    }

    //PARA ELIMINAR AL CONDUCTOR DE LA LOCALIZACION
        private fun salirEliminando(){
        finish()
        //ELIMINA LA DISPONIBILIDAD DEL CONDUCTOR*****YO****
        if (myLocationLatLng != null && !isMotoTrip && authProvider.getId()!= null) {
            if (authProvider.getId()!= ""){
                geoProvider.removeLocation(authProvider.getId())
            }
        }

        // DESCONECTAR MOTO
        if (myLocationLatLng!= null && isMotoTrip && authProvider.getId()!= null){
            if (authProvider.getId()!= "") {
                geoProvider.removeLocationMoto(authProvider.getId())
            }
        }

        easyWayLocation?.endUpdates()
        bookingListener?.remove()
        stopSensor()
    }


}