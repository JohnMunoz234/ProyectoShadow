package com.gse.aulapp.receptions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.core.util.toKotlinPair
import com.gse.aulapp.receptions.databinding.ActivitySelectorBiometricsBinding
import com.identy.*
import com.identy.enums.Finger
import com.identy.enums.Hand
import com.identy.enums.Template
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.util.*
import java.util.HashMap


class SelectorBiometrics : AppCompatActivity() {
    val TAG = "Selector -->>"
    lateinit var binding: ActivitySelectorBiometricsBinding
    lateinit var preferences: SharedPreferences
    lateinit var idUser: String
    lateinit var name: String
    lateinit var lastName: String
    lateinit var email: String
    lateinit var nameComplete : String
    val base64encoding = Base64.DEFAULT
    var operations = OperationsIdenty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectorBiometricsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        listener()
    }

    //
    private fun init(){
        val bundle = intent.extras
        idUser = bundle?.getString("id").toString()
        name = bundle?.getString("name").toString()
        lastName = bundle?.getString("lastname").toString()
        email = bundle?.getString("email").toString()


        nameComplete = name.plus(lastName).replace(" ","")
        Log.d(TAG,nameComplete)
        Log.e("Mensaje ==>>",idUser)
        preferences = getSharedPreferences("Huellas", Context.MODE_PRIVATE)


    }

    private fun listener(){

        binding.handsBiopmetric.setOnClickListener{ handsBiometric(this) }
        binding.faceBiometric.setOnClickListener{ faceBiometric() }
    }

    private fun handsBiometric(
        activity: Activity
    ){
        HandsBiometrics.registerHands(activity,object : IdentyResponseListener {
            override fun onAttempt(hand: Hand?, p1: Int, map: MutableMap<Finger, Attempt>?) {
                if (map != null) {

                    Log.d("NFIQ", map.get(Finger.INDEX)?.getNfiq1Score().toString())
                    Log.d("Spoof Score", map.get(Finger.INDEX)?.getSpoofScore().toString())
                    Log.d("Capture Time", map.get(Finger.INDEX)?.getCaptureTime().toString())
                    Log.d("Quality Failed?", map.get(Finger.INDEX)?.isQualityFailed().toString())
                    Log.d("Score Index?", map.get(Finger.INDEX)?.getScore().toString())
                    Log.d("Score Middle?", map.get(Finger.MIDDLE)?.getScore().toString())
                    Log.d("Score Ring?", map.get(Finger.RING)?.getScore().toString())
                    Log.d("Score Little?", map.get(Finger.LITTLE)?.getScore().toString())

                }
            }

            override fun onResponse(identyResponse: IdentyResponse?, hashSet: HashSet<String>?) {
                Log.e("Mensaje hashSet",hashSet.toString())

                HandsBiometrics.saveStringSet(hashSet!!.toSet(),activity)

                if (hashSet != null){

                    val response = identyResponse?.toJson(activity.applicationContext).toString()
                    val response2 = identyResponse?.action.toString()

                    try {

                        var printWriter = PrintWriter(File(getExternalFilesDir("/IDENTY/"),"/JSON_OUPTPUT.json"))

                        printWriter.write(identyResponse?.toJson(activity).toString())
                        printWriter.flush()
                        printWriter.close()
                        Log.e("Mensaje",response)

                        val editor = preferences.edit()
                        editor.putString(idUser,response)
                        editor.commit()

                    }catch (error: Exception){
                        error.printStackTrace()
                        Log.e("Mensaje","Error con el json")
                    }
                    // Codigo externo
                    var dir: File = createExternalDirectory("TEMPLATE_OUTPUT")
                    dir = File(dir,"user_${nameComplete}")
                    dir.mkdir()

                    // Manejo de las plantillas
                    for ( i in identyResponse?.prints?.entries!!){
                        var handFinger:Pair<Hand, Finger> = Pair(i.key.first,i.key.second)
                        var fingerOutput:FingerOutput = i.value

                        for ( t in Template.values()){
                            try {
                                Log.d(TAG,"onResponse: on template : ${t}" )
                                if (fingerOutput.templates.containsKey(t)){
                                    Log.d(TAG,"onResponse: tiene esta plantillas : ${t}")

                                    var templateSize = TemplateSize.values()
                                    for (ts in templateSize){
                                        Log.d(TAG,"onResponse: en tamaño : ${ts}")
                                        if (fingerOutput.templates.get(t)!!.containsKey(ts)){
                                            Log.d(TAG,"onResponse : en el tamaño de la plantilla : ${ts}")
                                            var base64Str = fingerOutput.templates.get(t)!!.get(ts)
                                            Log.d(TAG,"onResponse : base64Stre null -->> ${base64Str == null}")
                                            var base64decoded:String = Base64.decode(base64Str?.toByteArray(),base64encoding).toString()
                                            Log.d("BASE64DECODE",base64decoded)
                                            val fileNew = File(dir,"${getFileNamingConvention(handFinger.first,handFinger.second)}-${ts}.${t}")
                                            if (fileNew.exists()){
                                                fileNew.delete()
                                            }
                                            Log.d(TAG,fileNew.name)
                                            val outPut = FileOutputStream(fileNew)
                                            outPut.write(Base64.decode(base64Str?.toByteArray(),base64encoding))
                                            outPut.close()

                                        }
                                    }
                                }

                            }catch (ioe: Exception){
                                ioe.printStackTrace()
                            }

                        }
                        mover()
                    }
                }else {
                    Log.e("---->","hashSet is null")
                }
            }

            override fun onErrorResponse(identyError: IdentyError?, hashSet: HashSet<String>?) {

                if (hashSet != null){
                    Log.e("Error : ",identyError?.error.toString())
                }
            }

        },name.toString())

    }

    private fun faceBiometric(){
        Toast.makeText(this,"Has seleccionado las biometrias de la cara", Toast.LENGTH_SHORT).show()
    }

    private fun mover(){
        val intent:Intent = Intent(this, MainActivity::class.java)
        intent.putExtra("id",idUser)
        startActivity(intent)
    }


//--------------------------------------------------------------------------------------------------
// Codigo para crear carpetas y archivos externos
// Metodo para crear carpetas externas
    private fun createExternalDirectory(folder: String): File{
            val main_folder = getExternalFilesDir("/IDENTY/")

            val main_dir= File(main_folder.toString())
            if (!main_dir.exists()){
                main_dir.mkdir()
            }

            val dir_path = "$main_dir/$folder"
            val dir = File(dir_path)

            if (!dir.exists()){
                dir.mkdir()
            }

            return dir

    }

    private fun getFileNamingConvention(hand: Hand, finger: Finger):String{
        if (hand.equals(Hand.RIGHT)){
            if (finger.equals(Finger.INDEX)){
                return "02"
            } else if (finger.equals(Finger.MIDDLE)){
                return "03"
            } else if (finger.equals(Finger.RING)){
                return "04"
            } else if (finger.equals(Finger.LITTLE)){
                return "05"
            }
        }
        return ""
    }

}

