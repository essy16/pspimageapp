package com.pspgames.library.model


import com.google.gson.annotations.SerializedName

data class ModelGenre(
    @SerializedName("genre")
    var genre: String = "",
    @SerializedName("id")
    var id: String = "",
    var checked: Boolean = false
)