package com.pspgames.library.utils

import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.pspgames.library.R
import com.pspgames.library.App


object WallpaperSetUtils {

    @RequiresApi(Build.VERSION_CODES.N)
    fun setLockScreen(context: Context, imageUrl: String, callback: () -> Unit){
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .target(
                onSuccess = {
                    WallpaperManager.getInstance(context).setBitmap(
                        it.toBitmap(),
                        null,
                        true,
                        WallpaperManager.FLAG_LOCK
                    )
                    callback.invoke()
                },
                onError = {
                    App.toast(context.getString(R.string.wallpaper_set_failed))
                    callback.invoke()
                }
            )
            .build()
        context.imageLoader.enqueue(request)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun setHomeScreen(context: Context, imageUrl: String, callback: () -> Unit){
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .target(
                onSuccess = {
                    WallpaperManager.getInstance(context).setBitmap(
                        it.toBitmap(),
                        null,
                        true,
                        WallpaperManager.FLAG_SYSTEM
                    )
                    callback.invoke()
                },
                onError = {
                    App.toast(context.getString(R.string.wallpaper_set_failed))
                    callback.invoke()
                }
            )
            .build()
        context.imageLoader.enqueue(request)
    }
}