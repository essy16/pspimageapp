package com.pspgames.library.services

import android.app.WallpaperManager
import android.content.*
import android.media.MediaPlayer
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.io.File
import java.io.IOException


class VideoWallpaperService: WallpaperService() {
    internal inner class VideoEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null
        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            val videoFilePath = File(filesDir , "VIDEO/pspgames.mp4").absolutePath
            mediaPlayer = MediaPlayer().apply {
                setSurface(holder.surface)
                setDataSource(videoFilePath)
                isLooping = true
                setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                prepare()
                setVolume(0f, 0f)
                start()
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                mediaPlayer!!.start()
            } else {
                mediaPlayer!!.pause()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            if (mediaPlayer!!.isPlaying) mediaPlayer!!.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        override fun onDestroy() {
            super.onDestroy()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    companion object {
        fun start(context: Context) {
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, VideoWallpaperService::class.java)
                )
            }.also {
                context.startActivity(it)
            }
            try {
                WallpaperManager.getInstance(context).clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}