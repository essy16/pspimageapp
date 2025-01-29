package com.pspgames.library.model


import com.google.gson.annotations.SerializedName

data class ModelConsoleResult(
    @SerializedName("current_page")
    var currentPage: Int = 0,
    @SerializedName("data")
    var consoles: ArrayList<ModelLatest.Data> = arrayListOf(),
    @SerializedName("last_page")
    var lastPage: Int = 0,
    @SerializedName("total")
    var total: Int = 0
)