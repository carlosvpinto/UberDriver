package com.carlosvicente.uberdriverkotlin.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.carlosvicente.uberdriverkotlin.R


class SplashActivity : AppCompatActivity() {

    val DURACION : Long = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //ACULTAR TOOL BAR*****************************
        supportActionBar?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {   //verifica la version para usar el metodo mas actual
            // Utiliza el método setSystemBarsBehavior en versiones API 30 o superiores
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Manejar dispositivos con versiones de API más antiguas
            // Aquí puedes realizar acciones alternativas si es necesario
            this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        //****************************************************************




        val logo = findViewById<ImageView>(R.id.logoSplash)
        Glide.with(this).load(R.drawable.ic_logotaxiahora240x240svg).into(logo)

        cambiarActivity()
    }

//    private fun cambiarActivity() {
//        Handler().postDelayed(Runnable {
//            val intent = Intent(this,MainActivity::class.java)
//            startActivity(intent)
//        }, DURACION)
//    }
    private fun cambiarActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }, DURACION)
    }





}