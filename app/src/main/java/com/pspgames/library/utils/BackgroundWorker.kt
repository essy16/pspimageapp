package com.pspgames.library.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pspgames.library.App
import com.pspgames.library.enums.WallpaperTarget
import com.pixplicity.easyprefs.library.Prefs
import java.io.File
import kotlin.random.Random


class BackgroundWorker(
    applicationContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(applicationContext, workerParameters) {
    override suspend fun doWork(): Result {
        return try {
            try {
                App.log("Run work manager")
                runWallpaperChanger()
                Result.success()
            } catch (e: Exception) {
                App.log("exception in doWork ${e.message}")
                Result.failure()
            }
        } catch (e: Exception) {
            App.log("exception in doWork ${e.message}")
            Result.failure()
        }
    }

    private fun runWallpaperChanger() {
        val listImage = File(applicationContext.filesDir, "auto_wallpaper").listFiles()
        listImage?.let {
            try {
                val randomIndex = Random.nextInt(it.size)
                val randomImage = it[randomIndex]
                val bitmap = BitmapFactory.decodeFile(randomImage.absolutePath)
                val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                if(Prefs.getString("auto_wallpaper_target") == WallpaperTarget.HOME.name){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                } else if(Prefs.getString("auto_wallpaper_target") == WallpaperTarget.LOCK.name){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                } else {
                    wallpaperManager.setBitmap(bitmap)
                }
            } catch (e: Exception) {
                App.log(e)
            }
        }
    }
}
