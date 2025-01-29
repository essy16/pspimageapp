package com.pspgames.library.model

import java.io.Serializable

data class ModelCategories(
    val current_page: Int,
    val `data`: ArrayList<Data>,
    val last_page: Int,
) {
    data class Data(
        val created_at: String,
        val id: Int,
        val image: String,
        val name: String,
        val updated_at: String
    ) : Serializable
}