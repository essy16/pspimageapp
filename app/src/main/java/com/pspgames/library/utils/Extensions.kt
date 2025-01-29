package com.pspgames.library.utils

import android.util.Log
import android.view.View
import android.widget.ImageView
import coil.ImageLoader
import com.pspgames.library.model.ModelLatest
import okhttp3.OkHttpClient

fun View.hide() = apply { visibility = View.GONE }
fun View.show() = apply { visibility = View.VISIBLE }
fun ImageView.loadImage(){
    val imageLoader = ImageLoader.Builder(context)
        .okHttpClient {
            OkHttpClient.Builder()
                .build()
        }
        .build()
}
fun logging(any: Any?) = Log.e("ABENK : ", any.toString())
 fun ModelLatest.Data.isConsole() = form == "CONSOLE"