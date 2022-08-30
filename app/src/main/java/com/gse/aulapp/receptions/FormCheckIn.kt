package com.gse.aulapp.receptions

import android.R
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.edit
import com.google.gson.Gson
import com.gse.aulapp.receptions.databinding.ActivityFormCheckInBinding
import com.identy.users.IdentyAppDatabase
import org.json.JSONObject

import kotlin.properties.Delegates

class FormCheckIn : AppCompatActivity() {
    lateinit var binding: ActivityFormCheckInBinding
    lateinit var regex: Regex
    lateinit var preferences: SharedPreferences
    lateinit var prefeTwo: SharedPreferences
    lateinit var lista: Array<String>
    lateinit var jsonConverter:String
    var set = HashSet<String>()
    var list = ArrayList<String>()
    var idUser: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormCheckInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        listener()

    }

    override fun onBackPressed() {
        val intent: Intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun init(){
        regex = Regex("(?:[^<>()\\[\\].,;:\\s@\"]+(?:\\.[^<>()\\[\\].,;:\\s@\"]+)*|\"[^\\n\"]+\")@(?:[^<>()\\[\\].,;:\\s@\"]+\\.)+[^<>()\\[\\]\\.,;:\\s@\"]{2,63}")
        preferences = getSharedPreferences("Elementos", Context.MODE_PRIVATE)
        prefeTwo = getSharedPreferences("Listado",Context.MODE_PRIVATE)



        val valores = arrayOf(prefeTwo.getStringSet("Lista de ids",set).toString())

        lista = arrayOf("Selecciona Documento","Cedula de ciudadania","Tarjeta de identidad","Contraseña","Pasaporte")
        val adapter = ArrayAdapter<String>(this, R.layout.simple_list_item_1,lista)

        binding.spListaDocumentos.adapter = adapter

    }

    fun listener(){
        binding.btnRegister.setOnClickListener{validationForm()}
    }

    fun validationForm(){

        if (validationEmpty()){

            if (validationEmail()) {
                createJsonUser()

                list.add(binding.edtIdDocument.text.toString())

                set.addAll(list)
                val editor = preferences.edit()
               editor.putString("${binding.edtIdDocument.text}",jsonConverter)
               editor.commit()


                val editorTwo = prefeTwo.edit()
                editorTwo.putStringSet("Lista de ids",set)
                editorTwo.commit()

                OperationsIdenty.cargarUsuario(this,binding.edtFirstName.text.toString(),binding.edtEmail.text.toString())


                var intent: Intent = Intent(this, SelectorBiometrics::class.java)
                intent.putExtra("id",binding.edtIdDocument.text.toString())
                intent.putExtra("email",binding.edtEmail.text.toString())
                startActivity(intent)

                finish()
            } else {
                Toast.makeText(this,"Error al introducir el email !!!", Toast.LENGTH_SHORT).show()
            }
        }  else {
            Toast.makeText(this,"Existen campos vacios, por favor validar!!!", Toast.LENGTH_SHORT).show()
        }
    }


    fun validationEmail():Boolean{
        if ( !binding.edtEmail.editableText.toString().trim().matches(regex) ){
            return false
        } else {
            Toast.makeText(this,"Excelente tu correo es ${binding.edtEmail.editableText.toString()}",
                Toast.LENGTH_SHORT).show()
            return true
        }
    }

    private fun validationEmpty():Boolean{
        if (!binding.edtFirstName.text.toString().trim().isEmpty()){
            if (!binding.edtLastName.text.toString().trim().isEmpty()){
                if (!binding.edtEmail.text.toString().trim().isEmpty()){
                    if (!binding.edtIdDocument.text.toString().trim().isEmpty()){
                        if (!binding.edtPhone.text.toString().trim().isEmpty()){
                            if (binding.spListaDocumentos.selectedItem != lista.get(0))
                                return true
                        } else{
                            return false
                        }
                    } else{
                        return false
                    }
                } else {
                    return false
                }
            } else{
                return false
            }
        } else {
            return false
        }
        return false
    }

    private fun createJsonUser(){
        if ( idUser == 0){
            idUser++
            jsonConverter = "{" +
                    "\"name\": \"${binding.edtFirstName.text} \"," +
                    "\"apellido\": \"${binding.edtLastName.text}\"," +
                    "\"telefono\":\"${binding.edtPhone.text}\","+
                    "\"email\":\"${binding.edtEmail.text}\"," +
                    "\"tipo\":\"${binding.spListaDocumentos.selectedItem}\""+
                    "}"

        } else if (idUser > 0){
            idUser++
        }

    }
}