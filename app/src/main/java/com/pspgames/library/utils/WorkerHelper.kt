package com.pspgames.library.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pixplicity.easyprefs.library.Prefs
import java.util.concurrent.TimeUnit

object WorkerHelper {
    private const val JOB_NAME = "WallpaperChanger"
    fun enqueue(context: Context, forceUpdate: Boolean = false) {
        if (!Prefs.getBoolean("auto_wallpaper", false)) {
            cancel(context)
            return
        }

        val job = PeriodicWorkRequestBuilder<BackgroundWorker>(
            Prefs.getInt("auto_wallpaper_interval", 15).toLong(),
            TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        ).build()

        val policy = if (forceUpdate) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(JOB_NAME, policy, job)
    }

    private fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(JOB_NAME)
    }
}
