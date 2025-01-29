package com.pspgames.library.utils

import com.pspgames.library.model.ModelAdmob
import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs

object PrefsUtils {
    fun saveAdmob(modelAdmob: ModelAdmob){
        Prefs.putString("admob", Gson().toJson(modelAdmob))
    }
    fun getAdmob() : ModelAdmob {
        return Gson().fromJson(Prefs.getString("admob"), ModelAdmob::class.java)
    }
    fun saveFirst(){
        Prefs.putBoolean("isFirst", false)
    }
    fun getFirst() : Boolean {
        return Prefs.contains("isFirst")
    }
}