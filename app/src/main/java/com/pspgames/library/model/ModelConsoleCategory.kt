package com.pspgames.library.model


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ModelConsoleCategory(
    @SerializedName("id")
    var cid: String = "",
    @SerializedName("image")
    var image: String = "",
    @SerializedName("name")
    var name: String = ""
) : Serializable