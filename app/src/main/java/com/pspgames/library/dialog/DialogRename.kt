package com.pspgames.library.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.pspgames.library.App
import com.pspgames.library.R
import com.pspgames.library.databinding.DialogRenameBinding
import com.pspgames.library.model.ModelDownload
import java.io.File

@SuppressLint("SetTextI18n")
class DialogRename(private val context: Context, private val item: ModelDownload,private val callback: (filename: String) -> Unit): Dialog(context, R.style
    .AlertDialog) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val binding : DialogRenameBinding = DialogRenameBinding .inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        val file = item.createFile()
        binding.extension.text = "." + file.extension
        binding.filename.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                var str = s.toString()
                if (!(str.matches("[a-zA-Z0-9-_ ]*".toRegex()))) {
                    str = removeIllegalChar(str).trim() //trim whitespaces
                    binding.filename.setText(str)
                    binding.filename.setSelection(str.length) //use only if u want to set cursor to end
                }
            }
        })

        binding.filename.setOnEditorActionListener { textView, actionId, event ->
            if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN ))){
                rename(textView)
                dismiss()
                true
            }
            else{
                false
            }
        }
        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.submit.setOnClickListener {
            rename(binding.filename)
            dismiss()
        }
    }

    private fun removeIllegalChar(str: String): String {
        for (i in str.indices) {
            if (!(str[i].toString().matches("[a-zA-Z0-9-_ ]*".toRegex()))) {
                return str.substring(0, i) + str.substring(i + 1)
            }
        }
        return str
    }

    private fun rename(textView: TextView){
        val file = item.createFile()
        val result = textView.text.toString()
        if(result.isEmpty()){
            App.toast("filename cannot be empty")
            return
        }
        val filename = if(result.contains(".")){
            result
        } else {
            result + "." + file.extension
        }
        if(file.renameTo(File(file.parentFile, filename))){
            callback.invoke(filename)
            App.toast("rename successfully")
        } else {
            App.toast("rename error, please try again later")
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}