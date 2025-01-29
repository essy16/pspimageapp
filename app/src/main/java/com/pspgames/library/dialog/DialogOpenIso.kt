package com.pspgames.library.dialog

import android.content.Context

class DialogOpenIso (context: Context, path: String): BaseDialog(context, true) {
    init {
        setCancelable(true)
        setTitle("Cant open file!")
        setDescrtiption("No application to open this $path")
        setNegativeButton("Close"){
            dismiss()
        }
    }
}