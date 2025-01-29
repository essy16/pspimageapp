package com.pspgames.library.model

data class ModelDual (
    val cid: Int,
    val download: Int,
    var id: String,
    val image: String,
    val image_second: String,
    val premium: Int,
    val tags: String,
    val type: String,
    var view: Int
)