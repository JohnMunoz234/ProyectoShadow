package com.gse.aulapp.receptions

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import android.util.Pair
import android.util.Xml
import androidx.annotation.RequiresApi
import com.identy.*
import com.identy.enums.Finger
import com.identy.enums.FingerDetectionMode
import com.identy.enums.Hand
import com.identy.enums.Template
import com.identy.exceptions.NoDetectionModeException
import com.identy.users.IdentyUser
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Path
import java.util.HashMap
import kotlin.math.log


class HandsBiometrics {
    companion object{
        private var detectionMode: Array<FingerDetectionMode>? = null
        private var licenseFile: String= "130_com.gse.aulapp.receptions2023-02-20 00_00_00.lic"
        private var base64Encoding = Base64.DEFAULT
        private var displayboxes = false
        private var enableSpoofCheck = true
        private var compression = WSQCompression.WSQ_15_1

        private val NET_KEY = ""
        lateinit var operations:OperationsIdenty
        lateinit var user: IdentyUser


        fun registerHands(
            activity: Activity,
            listener: IdentyResponseListener,
            nombre: String
        ){

            user = OperationsIdenty.buscarUsuario(activity,nombre)

            val templatesConfig= HashMap<Template, HashMap<Finger, ArrayList<TemplateSize>>>()
            val fingerSize = HashMap<Finger, ArrayList<TemplateSize>>()

            val sizes = ArrayList<TemplateSize>()
            sizes.add(TemplateSize.DEFAULT)

            fingerSize[Finger.INDEX] = sizes
            fingerSize[Finger.MIDDLE] = sizes
            fingerSize[Finger.RING] = sizes
            fingerSize[Finger.LITTLE] = sizes

            templatesConfig[Template.PNG] = fingerSize
          //  templatesConfig[Template.ANSI_378_2004] = fingerSize
           templatesConfig[Template.WSQ] = fingerSize
             // templatesConfig[Template.ISO_19794_2] = fingerSize
           //  templatesConfig[Template.ISO_19794_4] = fingerSize
         //   templatesConfig[Template.RAW]= fingerSize


            detectionMode = arrayOf(FingerDetectionMode.R4F)


            try{
                IdentySdk.newInstance(
                    activity,
                    licenseFile,
                    { d ->
                        updateIntent(activity)
                        d.disableTraining()
                        d.base64EncodingFlag = base64Encoding
                        d.setDisplayImages(displayboxes)
                            .setAS(enableSpoofCheck)
                            .setRequiredTemplates(templatesConfig)
                            .setDisplayBoxes(displayboxes)
                            .setWSQCompression(compression)
                            .setDetectionMode(detectionMode)

                        d.setDebug(true)
                        d.isAllowTabletLandscape = true
                        d.setAllowHandChange(false)

                        d.setInlineGuide(false, InlineGuideOption(5,1))
                        d.setQC { true }
                        Log.e("Mensaje plantilla",templatesConfig.toString())
                        try {

                            d.setRequiredTemplates(templatesConfig)
                            d.capture()

                        }catch (error: NoDetectionModeException){
                            Log.e("Mensaje ==>",error.toString())
                            error.printStackTrace()
                        } catch (error:Exception){
                            Log.e("Mensaje ==>",error.toString())
                            error.printStackTrace()
                        }

                    },listener, NET_KEY,true,false)


            }catch (error: Exception){
                Log.e("Mensaje ==>",error.toString())
                error.printStackTrace()
            }

        }



        fun validationHands(
            activity: Activity,
            listener: IdentyResponseListener,
            nameComplete:String
        ){

            val templatesConfig= HashMap<Template, HashMap<Finger, ArrayList<TemplateSize>>>()
            val fingerSize = HashMap<Finger, ArrayList<TemplateSize>>()


            val sizes = ArrayList<TemplateSize>()

            sizes.add(TemplateSize.DEFAULT)

            fingerSize[Finger.INDEX] = sizes
            fingerSize[Finger.MIDDLE] = sizes
            fingerSize[Finger.RING] = sizes
            fingerSize[Finger.LITTLE] = sizes




            templatesConfig[Template.PNG] = fingerSize
           //  templatesConfig[Template.ANSI_378_2004] = fingerSize
            templatesConfig[Template.WSQ] = fingerSize
          //   templatesConfig[Template.ISO_19794_2] = fingerSize
        // templatesConfig[Template.ISO_19794_4] = fingerSize
          //  templatesConfig[Template.RAW]= fingerSize

            detectionMode = arrayOf(FingerDetectionMode.R4F)

            try {
                IdentySdk.newInstance(
                    activity,
                    licenseFile,
                    { d ->
                        updateIntent(activity)
                        d.disableTraining()
                        d.base64EncodingFlag = base64Encoding
                        d.setDisplayImages(displayboxes)
                            .setAS(enableSpoofCheck)
                            .setRequiredTemplates(templatesConfig)
                            .setDisplayBoxes(displayboxes)
                            .setWSQCompression(compression)
                            .setDetectionMode(detectionMode)


                        d.setDebug(true)
                        d.isAllowTabletLandscape = true
                        d.setAllowHandChange(false)

                        d.setQC { true }



                        try {
                            var template = HashMap<Pair<Hand,Finger>,String>()

                            template[Pair.create(Hand.RIGHT, Finger.INDEX)] =
                                activity.getExternalFilesDir("/IDENTY/TEMPLATE_OUTPUT/user_${nameComplete}/02-DEFAULT.WSQ").toString()

                            template[Pair(Hand.RIGHT, Finger.MIDDLE)] =
                                activity.getExternalFilesDir("/IDENTY/TEMPLATE_OUTPUT/user_${nameComplete}/03-DEFAULT.WSQ").toString()

                            template[Pair(Hand.RIGHT, Finger.RING)] =
                                activity.getExternalFilesDir("/IDENTY/TEMPLATE_OUTPUT/user_${nameComplete}/04-DEFAULT.WSQ").toString()

                            template[Pair(Hand.RIGHT, Finger.LITTLE)] =
                                activity.getExternalFilesDir("/IDENTY/TEMPLATE_OUTPUT/user_${nameComplete}/05-DEFAULT.WSQ").toString()

                            Log.e("templates",template.toString())
                            try {

                               val response =  d.verifyWithTemplates(Template.WSQ,template)
                                Log.e("Resultado", response.toString())


                            }catch (error: Exception){
                                Log.e("Mensaje ","Estoy aqui mirame")
                            error.printStackTrace()
                            }
                        }catch (error: NoDetectionModeException){
                            Log.e("Mensaje ==>",error.toString())
                            error.printStackTrace()
                        } catch (error:Exception){
                            Log.e("Mensaje ==>",error.toString())
                            error.printStackTrace()
                        }

                    },listener, "",true,false)
            }
           catch (error: Exception){
            Log.e("Mensaje ==>",error.toString())
            error.printStackTrace()
        }


    }

        private fun updateIntent(requiteActivity: Activity){
            val modes = ArrayList<FingerDetectionMode>()

            val finger: List<FingerDetectionMode>? = rightFingers(requiteActivity)
            if(finger.isNullOrEmpty() || finger.size == 4){
                modes.add(FingerDetectionMode.R4F)
            }else{
                modes.addAll(finger)
            }

            detectionMode = modes.toTypedArray()
        }

        private fun rightFingers(context: Context?): List<FingerDetectionMode>?{

            val selection = stringSet(context)
            Log.e("hashMet",selection.toString())
            val modes: MutableList<FingerDetectionMode> = ArrayList()

            if (selection!!.contains(FingerDetectionMode.RIGHT_INDEX.toString())){
                modes.add(FingerDetectionMode.RIGHT_INDEX)
            }
            if (selection!!.contains(FingerDetectionMode.RIGHT_MIDDLE.toString())){
                modes.add(FingerDetectionMode.RIGHT_MIDDLE)
            }
            if(selection!!.contains(FingerDetectionMode.RIGHT_RING.toString())){
                modes.add(FingerDetectionMode.RIGHT_RING)
            }
            if (selection!!.contains(FingerDetectionMode.RIGHT_LITTLE.toString())){
                modes.add(FingerDetectionMode.RIGHT_LITTLE)
            }

            return modes
        }


        private fun stringSet(context: Context?): Set<String>?{
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPreferences.edit()
            val key = "missing_fingers_v1"
            return sharedPreferences.getStringSet(key,HashSet())
        }


        fun saveStringSet(obj: Set<String>, context: Context?){
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPreferences.edit()
            val key = "missing_fingers_v1"

            if (obj.isEmpty()){
                editor.remove(key)
                editor.commit()
                return
            }
            editor.putStringSet(key,obj)
            editor.commit()


        }



    }
}
