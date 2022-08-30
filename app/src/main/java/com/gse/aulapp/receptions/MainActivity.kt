package com.gse.aulapp.receptions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.gse.aulapp.receptions.databinding.ActivityMainBinding
import com.identy.Attempt
import com.identy.IdentyError
import com.identy.IdentyResponse
import com.identy.IdentyResponseListener
import com.identy.enums.Finger
import com.identy.enums.Hand
import com.identy.users.IdentyUser
import org.json.JSONObject
import java.io.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var preferences: SharedPreferences
    lateinit var preferencesTwo: SharedPreferences
    var valor:String? = null
     var user: JSONObject? = null
    var nameBundle: String? = null
    var name: String? = null
    var lastName: String? = null
    var email:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        listener()


    }

    private fun init(){
        preferences = getSharedPreferences("Elementos", Context.MODE_PRIVATE)
        preferencesTwo = getSharedPreferences("Listado",Context.MODE_PRIVATE)

        val bundle = intent.extras
        nameBundle = bundle?.getString("nombre").toString()



        Log.e("Datos : ",preferences.all.toString().trim())
        Log.e("Arrays : ", preferencesTwo.all.toString().trim())
        permissions()
        permissionFiles()
    }

    private fun listener(){
        binding.txtRegister.setOnClickListener{ action() }
        binding.btnLogin2.setOnClickListener{ biometricHands(this,"100",2) }
        binding.btnLogin.setOnClickListener{ loginProcess()}
    }


    private fun loginProcess(){
        valor  = preferences.getString(binding.edtIdUser.text.toString(),"No existe").toString()

    if (binding.edtIdUser.text.toString().trim().isEmpty()){
            Toast.makeText(this,"El campo esta vacio, porfavor validar!!!", Toast.LENGTH_SHORT).show()
            errorBgLogin()
        } else {
            if (valor != "No existe"){
                user = JSONObject(valor)

                email = user?.getString("email")


                biometricHands(this, binding.edtIdUser.text.toString(),1)
            }else{
                Toast.makeText(this,"Error el id es incorrecto \nPor favor validar!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun action(){
        var intent: Intent = Intent(this, FormCheckIn::class.java)
        startActivity(intent)
        finish()
    }


    fun errorBgLogin(){
        binding.edtIdUser.background = getDrawable(R.drawable.edt_id_red)
        binding.edtIdUser.setPadding(55)
    }

    private fun biometricHands(activity: Activity,idUser: String, tipoLogin: Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HandsBiometrics.validationHands(activity,object : IdentyResponseListener {
                override fun onAttempt(hand: Hand?, p1: Int, map: MutableMap<Finger, Attempt>?) {
                    Log.d("NFIQ", map?.get(Finger.INDEX)?.getNfiq1Score().toString())
                    Log.d("Spoof Score", map?.get(Finger.INDEX)?.getSpoofScore().toString())
                    Log.d("Capture Time", map?.get(Finger.INDEX)?.getCaptureTime().toString())
                    Log.d("Quality Failed?", map?.get(Finger.INDEX)?.isQualityFailed().toString())
                    Log.d("Score Index?", map?.get(Finger.INDEX)?.getScore().toString())
                    Log.d("Score Middle?", map?.get(Finger.MIDDLE)?.getScore().toString())
                    Log.d("Score Ring?", map?.get(Finger.RING)?.getScore().toString())
                    Log.d("Score Little?", map?.get(Finger.LITTLE)?.getScore().toString())
                }

                override fun onResponse(identyResponse: IdentyResponse?, hashSet: HashSet<String>?) {
                Log.e("Mensaje",hashSet.toString())
                    if (hashSet != null){
                        val response = identyResponse?.toJson(activity.baseContext).toString()
                        val response2 = identyResponse?.action.toString()
                        try {

                            val jsonData = JSONObject(response)
                            Log.e("Mensaje Validation ==>>",response)




                            Log.e("Mensaje acción ==>>",response2)

                            moverWelcome()
                        }catch (error: Exception){
                            error.printStackTrace()
                            Log.e("Mensaje ==>","Error con el json")
                        }
                    }else {
                        Log.e("Mensaje ==>","hashSet is null")
                    }
                }

                override fun onErrorResponse(identyError: IdentyError?, hashSet: HashSet<String>?) {

                    if (hashSet != null){
                        Log.e("Mensaje Error ==>","Error ${identyError?.error.toString()}")
                    }
                }

            },idUser,tipoLogin)
        }

    }

    fun permissions(){
        try {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions((this), arrayOf(android.Manifest.permission.CAMERA),100)
            }
        }catch (error: Exception){
            error.printStackTrace()
        }
    }

    fun permissionFiles(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Verifica permisos para Android 6.0+
            val permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.i("Mensaje", "No se tiene permiso para leer.")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    225
                )
            } else {
                Log.i("Mensaje", "Se tiene permiso para leer!")
            }
        }
    }

    fun moverWelcome(){

        Toast.makeText(this,"Login correcto ha ingresado el usuario $email",
            Toast.LENGTH_SHORT).show()

        val intent = Intent(this,welcomeActivity::class.java)
        startActivity(intent)
    }



}

