package com.pspgames.library.utils

import com.pspgames.library.model.ModelDownload
import com.google.gson.Gson

object DataUtils {
    fun encrypt(modelDownload: ModelDownload) : String{
        return Gson().toJson(modelDownload)
    }

    fun decrypt(json: String) : ModelDownload {
        return Gson().fromJson(json, ModelDownload::class.java)
    }
}