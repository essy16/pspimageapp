package com.pspgames.library.model


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ModelColor(
    @SerializedName("color")
    val color: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
) : Serializable