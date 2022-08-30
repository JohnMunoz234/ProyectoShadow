package com.gse.aulapp.receptions.util

import android.app.Activity
import android.app.AlertDialog
import com.gse.aulapp.receptions.R

class LoadingDialog(val activity: Activity) {
        lateinit var dialog: AlertDialog


        fun startLoading(){
            val inflater = activity.layoutInflater
            val dialogView = inflater.inflate(R.layout.loading_item,null)
            val builder = AlertDialog.Builder(activity)

            builder.setView(dialogView)
            builder.setCancelable(false)
            dialog = builder.create()
            dialog.show()
            timeLoading()
        }

        fun dismissLoading(){
            dialog.dismiss()
        }

        fun timeLoading(){
            val handler = android.os.Handler()
            handler.postDelayed(object :Runnable{
                override fun run() {
                    dismissLoading()
                }
            },5000)
        }

}