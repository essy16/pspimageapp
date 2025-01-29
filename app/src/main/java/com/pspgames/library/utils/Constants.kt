package com.pspgames.library.utils

import com.pspgames.library.model.ModelDownload
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("ConstPropertyName")
object Constants {
    const val workerName = "downloadWorker"
    const val notificationChannelName: String = "WorkManager Notifications"
    const val notificationChannelDescription = "Shows notifications whenever work events occur"
    const val notificationTitle = "Download Request Status"
    const val notificationChannelId = "work manager channel"
    //var lastWorkId: UUID?  = null

    val enqueuedDownloads = MutableStateFlow<MutableSet<ModelDownload>>(mutableSetOf())
}