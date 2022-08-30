package com.gse.aulapp.receptions

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.identy.IdentyMatcher
import com.identy.users.IdentyUser
import com.identy.users.IdentyUserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OperationsIdenty{
    companion object{
        var user:IdentyUser? = null

        fun cargarUsuario(activity: Activity,name:String,email:String){
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    IdentyUserManager.getInstance(activity).createUser(name,email)
                }
            }catch (error: Exception){
                Toast.makeText(activity, "El error es porque ya existe le usuario",Toast.LENGTH_SHORT).show()
            }
        }

        fun buscarUsuario(activity: Activity,name: String):IdentyUser{
            try {
                user = IdentyUser()
                Log.e("Mensaje", name)
                CoroutineScope(Dispatchers.IO).launch {
                    user = IdentyUserManager.getInstance(activity).getUserByuserName(name)
                    Log.e("Mensaje", user?.username.toString())
                }
            }catch (error: Exception){
                Toast.makeText(activity, "No se encontro el usuario, el campo estaba vacio !!",Toast.LENGTH_SHORT).show()
            }

            return user!!
        }


        fun traerTodo(activity: Activity){
            CoroutineScope(Dispatchers.IO).launch {
                IdentyUserManager.getInstance(activity).allUsers
            }
        }

    }

}