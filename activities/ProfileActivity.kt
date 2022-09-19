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
import com.carlosvicente.uberdriverkotlin.providers.AuthProvider
import com.carlosvicente.uberdriverkotlin.providers.DriverProvider
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


    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

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

        if (imageFile != null) {
            driverProvider.uploadImage(authProvider.getId(), imageFile!!).addOnSuccessListener { taskSnapshot ->
                driverProvider.getImageUrl().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    driver.image = imageUrl
                    driverProvider.update(driver).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        else {
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
                    Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                }
                else {
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


