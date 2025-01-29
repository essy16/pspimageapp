package com.pspgames.library.dialog

import android.content.Context
import com.pspgames.library.R

class DialogTheme(context: Context, onRestart: () -> Unit): BaseDialog(context, true) {
    init {
        setCancelable(true)
        setTitle(context.getString(R.string.dialog_theme_title))
        setDescrtiption(context.getString(R.string.dialog_theme_description))
        setPositiveButton(context.getString(R.string.dialog_theme_button1)){
            dismiss()
            onRestart.invoke()
        }
        setNegativeButton(context.getString(R.string.dialog_theme_button2)){
            dismiss()
        }
    }
}