package com.pspgames.library.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.pspgames.library.R

open class BaseDialog(context: Context, cancelable: Boolean? = false): Dialog(context, R.style.AlertDialog) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setContentView(R.layout.dialog_base)
        setCancelable(cancelable!!)
    }

    fun setTitle(text: String){
        val dialogTitle = findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.visibility = View.VISIBLE
        dialogTitle.text = text
    }

    fun setDescrtiption(text: String){
        val dialogDescription = findViewById<TextView>(R.id.dialogDescription)
        dialogDescription.visibility = View.VISIBLE
        dialogDescription.text = text
    }

    fun setPositiveButton(text: String, callback: (Dialog) -> Unit){
        val dialogButtonPositive = findViewById<Button>(R.id.dialogButtonPositive)
        dialogButtonPositive.visibility = View.VISIBLE
        dialogButtonPositive.text = text
        dialogButtonPositive.setOnClickListener {
            callback.invoke(this)
        }
    }

    fun setNegativeButton(text: String, callback: (Dialog) -> Unit){
        val dialogButtonNegative = findViewById<Button>(R.id.dialogButtonNegative)
        dialogButtonNegative.visibility = View.VISIBLE
        dialogButtonNegative.text = text
        dialogButtonNegative.setOnClickListener {
            callback.invoke(this)
        }
    }
}
