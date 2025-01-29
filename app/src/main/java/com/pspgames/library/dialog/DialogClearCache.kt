package com.pspgames.library.dialog

import android.content.Context
import com.pspgames.library.R

class DialogClearCache(context: Context, onClear: () -> Unit): BaseDialog(context, true) {
    init {
        setCancelable(true)
        setTitle(context.getString(R.string.dialog_clear_title))
        setDescrtiption(context.getString(R.string.dialog_clear_description))
        setPositiveButton(context.getString(R.string.dialog_clear_button1)){
            dismiss()
            onClear.invoke()
        }
        setNegativeButton(context.getString(R.string.dialog_clear_button2)){
            dismiss()
        }
    }
}