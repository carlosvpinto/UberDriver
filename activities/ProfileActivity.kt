package com.carlosvicente.uberdriverkotlin.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.carlosvicente.uberdriverkotlin.R
//import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.protobuf.Empty
import com.carlosvicente.uberdriverkotlin.databinding.ActivityProfileBinding
import com.carlosvicente.uberdriverkotlin.models.Driver
import com.carlosvicente.uberdriverkotlin.models.ReciboConductor
import com.carlosvicente.uberdriverkotlin.providers.AuthProvider
import com.carlosvicente.uberdriverkotlin.providers.DriverProvider
import com.carlosvicente.uberdriverkotlin.providers.ReciboCondutorlProvider
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import java.io.File
import kotlin.math.log

class ProfileActivity : AppCompatActivity(),RadioGroup.OnCheckedChangeListener {

    private lateinit var binding: ActivityProfileBinding
    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()
    var RadioGrup :RadioGroup? = null
    var Tipo: String? = "Carro"
    var radioGroup: RadioGroup? = null
    var optcarro : RadioButton? = null
    var optmoto : RadioButton? = null
    private var reciboConductor = ArrayList<ReciboConductor>()
    private var reciboConductorProvider = ReciboCondutorlProvider()
    private var recibosConductores = ArrayList<ReciboConductor>()


    private var totalBs = 0.0
    private var totalDollar= 0.0
    private var totalSinVeriBs = 0.0
    private var totalSinVeriBsDollar = 0.0

    private var progressDialog = ProgressDialogFragment


    private var imageFile: File? = null
    private var imageFileVehiculo: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        progressDialog.showProgressBar(this)
        getDriver()

        radioGroup = findViewById(R.id.GrupoOpt)
        optcarro = findViewById(R.id.optcarro)
        optmoto = findViewById(R.id.optmoto)
        radioGroup?.setOnCheckedChangeListener(this)
        binding.imageViewBack.setOnClickListener { finish() }
        binding.btnUpdate.setOnClickListener { updateInfo() }
        binding.circleImageProfile.setOnClickListener { selectImage() }


    }




    private fun updateInfo() {
        progressDialog.showProgressBar(this)

        val name = binding.textFieldName.text.toString()
        val lastname = binding.textFieldLastname.text.toString()
        val phone = binding.textFieldPhone.text.toString()
        val carBrand = binding.textFieldCarBrand.text.toString()
        val carColor = binding.textFieldCarColor.text.toString()
        val carPlate = binding.textFieldCarPlate.text.toString()
        




        val driver = Driver(
            id = authProvider.getId(),
            name = name,
            lastname = lastname,
            phone = phone,
            colorCar = carColor,
            brandCar = carBrand,
            plateNumber = carPlate,
            tipo = Tipo.toString()
        )
    //VALIDA LA INFORMACION DE LA IMAGEN DE PERFIL
        if (imageFile != null ) {
            driverProvider.uploadImage(authProvider.getId(), imageFile!!).addOnSuccessListener { taskSnapshot ->
                driverProvider.getImageUrl().addOnSuccessListener { url ->
                    val imageUrl = url.toString()

                    driver.image = imageUrl
                    driverProvider.update(driver).addOnCompleteListener {
                        if (it.isSuccessful) {
                            progressDialog.hideProgressBar(this)
                            Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        else {
                            progressDialog.hideProgressBar(this)
                            Toast.makeText(this@ProfileActivity, "No se pudo actualizar la informacion", Toast.LENGTH_LONG).show()
                        }
                    }
                    Log.d("STORAGE", "$imageUrl")
                }
            }

        }
        else {
            driverProvider.update(driver).addOnCompleteListener {
                if (it.isSuccessful) {
                    progressDialog.hideProgressBar(this)
                    Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                }
                else {
                    progressDialog.hideProgressBar(this)
                    Toast.makeText(this@ProfileActivity, "No se pudo actualizar la informacion", Toast.LENGTH_LONG).show()
                }
            }
        }


    }

    private fun getDriver() {
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()) {
                val driver = document.toObject(Driver::class.java)
                binding.textViewEmail.text = driver?.email
                binding.textFieldName.setText(driver?.name)
                binding.textFieldLastname.setText(driver?.lastname)
                binding.textFieldPhone.setText(driver?.phone)
                binding.textFieldCarBrand.setText(driver?.brandCar)
                binding.textFieldCarColor.setText(driver?.colorCar)
                binding.textFieldCarPlate.setText(driver?.plateNumber)
                totalizaPagos()
                if (driver?.tipo.toString() != "Carro"){
                    optmoto?.isChecked = true
                    Log.d("RADIO","MOTO")
                }
                if (driver?.tipo.toString()!="Moto"){
                    Log.d("RADIO","CARRO")
                    optcarro?.isChecked = true
                }

                if (driver?.image != null) {
                    if (driver.image != "") {
                        Glide.with(this).load(driver.image).into(binding.circleImageProfile)
                    }
                }
            }
            progressDialog.hideProgressBar(this)
        }
    }
    private fun totalizaPagos(){
        reciboConductor.clear()
        Log.d("PAGOMOVIL", "getPagosMoviles: ")
        var total = 0.0
        reciboConductorProvider.getReciboConductor(authProvider.getId()).get().addOnSuccessListener { query ->
            Log.d("PAGOMOVIL", "authProviderA: ${authProvider.getId()}")
            if (query != null) {
                if (query.documents.size > 0) {
                    val documents = query.documents

                    for (d in documents) {
                        var reciboConductor = d.toObject(ReciboConductor::class.java)
                        reciboConductor?.id = d.id
                        recibosConductores.add(reciboConductor!!)
                        if (reciboConductor.verificado != true) {
                            Log.d("COUNTAR", "ADENTRO ADETRO VERIFICADO FALSE:${reciboConductor.verificado} y $totalDollar ")
                            totalSinVeriBs += reciboConductor.montoBs!!.toDouble()
                            totalSinVeriBsDollar += reciboConductor.montoDollar!!.toDouble()
                        }

                        if (reciboConductor.verificado != false) {
                            Log.d("COUNTAR", "ADENTRO VERIFICADO TRUE: ${reciboConductor.verificado} y $totalDollar ")
                            totalBs += reciboConductor.montoBs!!.toDouble()
                            totalDollar += reciboConductor.montoDollar!!.toDouble()
                        }
                    }
                }
            }
            val totalVerdes = totalDollar
            binding.textViewBilletera.text=totalVerdes.toString()
            progressDialog.hideProgressBar(this)
            updateBilletera(authProvider.getId(),totalVerdes)
        }

    }
    //ACTUALIZA EL EL MONTO EN LA BILLETERA
    private fun updateBilletera(idDocument: String,totalDolar: Double) {
        driverProvider.updateBilleteraDriver(idDocument, totalDolar).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("BILLETERA", "totalDollarUpdate: ${totalDolar} ")
            }
            else {
                Log.d("BILLETERA", "FALLO ACTUALIZACION ${totalDolar} ")
            }
        }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data
            imageFile = File(fileUri?.path)
            binding.circleImageProfile.setImageURI(fileUri)
        }
        else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(this, "Tarea cancelada", Toast.LENGTH_LONG).show()
        }

    }





    private fun selectImage() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }


    override fun onCheckedChanged(group: RadioGroup?, IdRadio: Int) {
    when (IdRadio){
        optmoto?.id-> Tipo = "Moto"
        optcarro?.id-> Tipo = "Carro"
    }

    }

}


