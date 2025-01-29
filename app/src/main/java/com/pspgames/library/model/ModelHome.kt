package com.pspgames.library.model


import com.google.gson.annotations.SerializedName

data class ModelHome(
    @SerializedName("data")
    val `data`: ArrayList<ModelLatest.Data>,
    @SerializedName("name")
    val name: String
)
