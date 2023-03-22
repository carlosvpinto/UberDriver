package com.carlosvicente.uberdriverkotlin.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.carlosvicente.uberdriverkotlin.databinding.ActivityMainBinding
import com.carlosvicente.uberdriverkotlin.providers.AuthProvider
import com.tommasoberlose.progressdialog.ProgressDialogFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val authProvider = AuthProvider()
    private var progressDialog = ProgressDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //verifica la version del sistema operativo
        // val version = Build.VERSION.SDK_INT
        // Log.d("VERSION", "Android version is $version")
        // Toast.makeText(this, "Sistema operativo ${version}", Toast.LENGTH_SHORT).show()


        //PARA SOLICITAR PERMISO DE SUPERPOSICION***********
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
//            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
//            startActivity(intent)
//        }
        //**************************
        val version = Build.VERSION.SDK_INT
        if (Build.VERSION.SDK_INT < 28) {
            Toast.makeText(this, "Sistema operativo no esta actualizado para funcionar la app ${version}", Toast.LENGTH_LONG).show()
            finishAffinity()
        } else {
           // Toast.makeText(this, "Sistema operativo superior a Oreo ${version}", Toast.LENGTH_SHORT).show()
            //finishAndRemoveTask()
        }

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.btnRegister.setOnClickListener { goToRegister() }
        binding.btnLogin.setOnClickListener { login() }
    }



    private fun login() {
        progressDialog.showProgressBar(this) //ACTIVA EL PROGRESSBAR
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()

        if (isValidForm(email, password)) {
            authProvider.login(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                    goToMap()
                    progressDialog.hideProgressBar(this) //DESACTIVA EL PROGRESSBAR
                }
                else {
                    progressDialog.hideProgressBar(this) //DESACTIVA EL PROGRESSBAR
                    Toast.makeText(this@MainActivity, "CONTRASEÑA INCORRECTA:  ${it.exception.toString()}", Toast.LENGTH_LONG).show()
                    Log.d("FIREBASE", "ERROR: ${it.exception.toString()}")
                }
            }
        }
    }
    private fun goToMap() {
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun isValidForm(email: String, password: String): Boolean {

        if (email.isEmpty()) {
            Toast.makeText(this, "Ingresa tu correo electronico", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun goToRegister() {
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    override fun onStart() {
        super.onStart()
        if (authProvider.existSession()) {
            goToMap()
        }
    }
}