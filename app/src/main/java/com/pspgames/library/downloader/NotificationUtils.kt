package com.pspgames.library.downloader

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pspgames.library.R
import com.pspgames.library.utils.Constants

object NotificationUtils {

    @SuppressLint("MissingPermission")
    fun sendStatusNotification(message: String, context: Context, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Constants.notificationChannelName
            val description = Constants.notificationChannelDescription
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(Constants.notificationChannelId, name, importance)
            channel.description = description
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, Constants.notificationChannelId)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentInfo(message)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }


    @SuppressLint("MissingPermission")
    fun sendUnzipProgress(message: String, context: Context, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Constants.notificationChannelName
            val description = Constants.notificationChannelDescription
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(Constants.notificationChannelId, name, importance)
            channel.description = description
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, Constants.notificationChannelId)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentInfo(message)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))
            .setProgress(100, 0, true)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    @SuppressLint("MissingPermission")
    fun sendUnzipSuccess(message: String, context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancelAll()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Constants.notificationChannelName
            val description = Constants.notificationChannelDescription
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(Constants.notificationChannelId, name, importance)
            channel.description = description
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, Constants.notificationChannelId)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentInfo(message)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}