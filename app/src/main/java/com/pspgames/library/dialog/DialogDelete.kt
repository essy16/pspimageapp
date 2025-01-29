package com.pspgames.library.dialog

import android.content.Context
import com.pspgames.library.R

class DialogDelete (context: Context, ondDelete: () -> Unit): BaseDialog(context, true) {
    init {
        setCancelable(true)
        setTitle(context.getString(R.string.dialog_delete_title))
        setDescrtiption(context.getString(R.string.dialog_delete_description))
        setPositiveButton(context.getString(R.string.dialog_delete_button2)){
            dismiss()
        }
        setNegativeButton(context.getString(R.string.dialog_delete_button1)){
            dismiss()
            ondDelete.invoke()
        }
    }
}