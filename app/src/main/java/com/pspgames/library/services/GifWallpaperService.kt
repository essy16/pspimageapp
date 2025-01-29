package com.pspgames.library.services

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import app.redwarp.gif.android.GifDrawable

import java.io.*

class GifWallpaperService : WallpaperService() {
    companion object {
        fun start(context: Context){
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(context, GifWallpaperService::class.java))
            intent.putExtra("SET_LOCKSCREEN_WALLPAPER", true)
            context.startActivity(intent)
            try {
                WallpaperManager.getInstance(context).clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    override fun onCreateEngine(): Engine {
        return CustomEngine()
    }

    inner class CustomEngine : Engine() {
        private var surfaceDrawableRenderer: SurfaceDrawableRenderer? = null
        private val handlerThread = HandlerThread("WallpaperLooper")
        private var handler: Handler? = null
        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            handlerThread.start()
            handler = Handler(handlerThread.looper)
            surfaceDrawableRenderer = SurfaceDrawableRenderer(surfaceHolder, handlerThread.looper)
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath, "image.gif")
            GifDrawable.from(file).map {
                it.start()
                surfaceDrawableRenderer?.drawable = it
            }
        }


        override fun onDestroy() {
            super.onDestroy()
            handlerThread.quit()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                handlerThread.start()
            } else {
                handlerThread.quitSafely()
            }
            surfaceDrawableRenderer?.visibilityChanged(visible)
        }
    }
}