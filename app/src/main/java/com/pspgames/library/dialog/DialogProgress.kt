package com.pspgames.library.dialog

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Window
import com.pspgames.library.R
import com.pspgames.library.databinding.DialogProgressBinding

class DialogProgress(context: Context): Dialog(context, R.style.AlertDialog) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val binding : DialogProgressBinding = DialogProgressBinding .inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(false)
    }

    override fun dismiss() {
        Handler(Looper.myLooper()!!).postDelayed({
            try {
                super.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 1000)
    }
}