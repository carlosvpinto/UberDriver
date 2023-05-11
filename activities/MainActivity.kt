package com.carlosvicente.uberkotlin.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.carlosvicente.uberkotlin.R
import com.carlosvicente.uberkotlin.databinding.ActivityMainBinding
import com.carlosvicente.uberkotlin.models.Client
import com.carlosvicente.uberkotlin.providers.AuthProvider
import com.carlosvicente.uberkotlin.providers.ClientProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import java.net.URL
import java.util.regex.Pattern


enum class ProviderType{
    BASIC,
    GOOGLE
}
class MainActivity : AppCompatActivity() {


    private var googleMap: GoogleMap? = null
    private var originLatLng: LatLng? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var binding: ActivityMainBinding
    val authProvider = AuthProvider()
    private val clientProvider= ClientProvider()

    private var progressDialog = ProgressDialogFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        //VERIFICA LA VERSION DEL SISTEMA OPERATIVO
        val version = Build.VERSION.SDK_INT
        if (Build.VERSION.SDK_INT < 28) {
            Toast.makeText(this, "Sistema operativo desactualizado Version Actual: ${version}", Toast.LENGTH_LONG).show()
            finishAffinity()
        } else {
            //Toast.makeText(this, "Sistema operativo superior a Oreo ${version}", Toast.LENGTH_SHORT).show()
            //finishAndRemoveTask()
        }

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //PARA AUTENTICAR CON GOOGLE
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this , gso)


        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        binding.ImgGoogle.setOnClickListener{
            signInGoogle()
        }
        binding.btnRegister.setOnClickListener { goToRegister() }
        binding.btnLogin.setOnClickListener { login() }
        binding.txtOlvideClave.setOnClickListener{olvideClave() }

    }

    private fun olvideClave() {

            val i = Intent(this, RecuperarClaveActivity::class.java)

            startActivity(i)

    }


    //PARA INICIAR CON GOOGLE
    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            Log.d("GOOGLE", "VALOS DE RESULT EN LAUNCHER ${result.resultCode}")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            Log.d("GOOGLE", "VER QUE handleResults: ${task.isSuccessful} y task.result ${task.result} ")
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                updateUI(account)
            }
        }else{
            Toast.makeText(this, task.exception.toString() , Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("GOOGLE", "VER updateUI it.isSuccessful: ${it.isSuccessful} ")
                val intent: Intent = Intent(this, MapActivity::class.java)
                val client = Client(
                    id = authProvider.getId(),
                    name = account.givenName,
                    lastname = account.familyName,
                    phone = "Agrere su Tlf",
                    email = account.email,
                    image = account.photoUrl.toString()
                )

                Log.d("FIREBASE", "VER QUE TRAE ACCONT: ${account}")
                /******
                 *     val id: String? = null,
                val name: String? = null,
                val lastname: String? = null,
                val email: String? = null,
                val phone: String? = null,
                var image: String? = null,
                var token: String? = null,
                 *
                 */

                clientProvider.create(client).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this@MainActivity,"Registro exitoso", Toast.LENGTH_SHORT).show()
                        progressDialog.hideProgressBar(this)
                        goToMap()
                    } else {
                        progressDialog.hideProgressBar(this)
                        Toast.makeText(this@MainActivity,"Hubo un error Almacenado los datos del usuario ${it.exception.toString()}",
                            Toast.LENGTH_SHORT).show()
                        Log.d("FIREBASE", "Error: ${it.exception.toString()}")
                    }
                }
                intent.putExtra("email", account.email)
                intent.putExtra("name", account.displayName)
                startActivity(intent)
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()


            }
        }
    }

    private fun login() {

        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()

        if (isValidForm(email, password)) {
            authProvider.login(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                   goToMap()
                    progressDialog.hideProgressBar(this)
                }
                else {
                    progressDialog.hideProgressBar(this)
                    Toast.makeText(this@MainActivity, "ERROR INICANDO SECION", Toast.LENGTH_SHORT).show()

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
            progressDialog.hideProgressBar(this@MainActivity)
            Toast.makeText(this, "Ingresa tu correo electronico", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            progressDialog.hideProgressBar(this@MainActivity)
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