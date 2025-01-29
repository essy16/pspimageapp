package com.pspgames.library.downloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pspgames.library.*
import com.pspgames.library.database.DownloadTable
import com.pspgames.library.enums.DownloadStatus
import com.pspgames.library.interfaces.DownloadListener
import com.pspgames.library.model.ModelDownload
import com.pspgames.library.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicBoolean

class DownloadWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val downloadTable: DownloadTable = DownloadTable.getRepo(context)
    private val downloadServices: DownloadServices = DownloadClient.createService()
    private val progress = 0


    companion object {
        const val DOWNLOAD_ID = "DOWNLOAD_ID"
        const val DOWNLOAD_PROGRESS = "DOWNLOAD_PROGRESS"
        const val DOWNLOAD_PAUSED = "DOWNLOAD PAUSED"
        const val DOWNLOAD_CURRENT = "DOWNLOAD_CURRENT"
        const val DOWNLOAD_TOTAL = "DOWNLOAD_TOTAL"
        const val DOWNLOAD_STATUS = "DOWNLOAD_STATUS"
        private var downloadListener: DownloadListener? = null
        private var modelDownload: ModelDownload = ModelDownload()
        private val running = AtomicBoolean(false)
        private val canceling = AtomicBoolean(false)
        private val paused = AtomicBoolean(false)
        private var currentDownloadPosition = 0L
        private var resumePosition = 0L

        private fun isRunning() = running.get()
        private fun isCanceling() = canceling.get()
        fun setListener(downloadListener: DownloadListener) {
            this.downloadListener = downloadListener
        }

        fun start(context: Context, observer: Observer<List<WorkInfo?>?>) {
            if (!isRunning()) {
                canceling.set(false)
                removeObserver(context, observer)
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val downloadRequest = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                    .setConstraints(constraints)
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    Constants.workerName,
                    ExistingWorkPolicy.REPLACE,
                    downloadRequest
                )
                addObserver(context, observer)
            }
        }

        fun addObserver(context: Context, observer: Observer<List<WorkInfo?>?>) {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkLiveData(Constants.workerName)
                .observeForever(observer)
        }

        fun removeObserver(context: Context, observer: Observer<List<WorkInfo?>?>) {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkLiveData(Constants.workerName)
                .removeObserver(observer)
        }

        fun cancel(context: Context, observer: Observer<List<WorkInfo?>?>) {
            running.set(false)
            WorkManager.getInstance(context).cancelAllWork()
            canceling.set(true)
            modelDownload.status = DownloadStatus.CANCELED.name
            downloadListener?.onCancel(modelDownload)

            start(context, observer)
        }

        fun pause() {
            paused.set(true)
            modelDownload.status = DownloadStatus.PAUSED.name
            downloadListener?.onPause(modelDownload)
        }

        fun resume(context: Context, observer: Observer<List<WorkInfo?>?>) {
            if (modelDownload.downloaded > 0) {
                resumePosition = modelDownload.downloaded
            }
            paused.set(false)
            modelDownload.status = DownloadStatus.DOWNLOADING.name
            downloadListener?.onResume(modelDownload)
            val resumePosition = modelDownload.downloaded

            // Create new work request with resume data
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData = Data.Builder()
                .putLong("resume_position", resumePosition)
                .build()

            val downloadRequest = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                Constants.workerName,
                ExistingWorkPolicy.REPLACE,
                downloadRequest
            )

            addObserver(context, observer)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Get resume position from input data
            val resumePosition = inputData.getLong("resume_position", 0L)
            currentDownloadPosition = resumePosition

// Make sure the notification is being updated correctly in the download loop.
            setForegroundAsync(createForegroundInfo("Downloading: $progress%"))

            // Ensure we're using a foreground service
            val foregroundInfo = createForegroundInfo("Starting download...")
            setForeground(foregroundInfo)

            performWork()
            running.set(false)
            Result.success()
        } catch (error: Exception) {
            updateProgress(if (isCanceling()) DownloadStatus.CANCELED else DownloadStatus.FAILED)
            running.set(false)
            delay(2000)
            Result.failure()
        }
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val channelId = "download_channel"
        val channelName = "Downloads"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification: Notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Downloading")
            .setContentText(progress)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()

        // Return ForegroundInfo with appropriate type for Android 14+
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(1, notification)
        }
    }
    private suspend fun performWork() {
        while (getPendingDownloads().isNotEmpty()) {
            modelDownload = getPendingDownload()
            downloadFile()
            delay(1000)
        }
    }

    private fun createFile(modelDownload: ModelDownload): File {
        val destinationFile = File(modelDownload.directory, modelDownload.filename)
        if (!destinationFile.exists()) {
            destinationFile.createNewFile()
        }
        return destinationFile
    }

    private suspend fun downloadFile() {
        running.set(true)
        val destinationFile = withContext(Dispatchers.IO) {
            createDir(modelDownload)
            createFile(modelDownload)
        }

        try {
            val response = if (currentDownloadPosition > 0) {
                // Add proper headers for range request
                downloadServices.downloadFileWithRange(
                    modelDownload.url,
                    "bytes=$currentDownloadPosition-"
                )
            } else {
                downloadServices.downloadFile(modelDownload.url)
            }

            response.run {
                byteStream().use { inputStream ->
                    FileOutputStream(destinationFile, currentDownloadPosition > 0).use { outputStream ->
                        val total = contentLength() + currentDownloadPosition
                        var downloaded = currentDownloadPosition
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytes = inputStream.read(buffer)

                        // Define the progress here
                        var progress: Int

                        while (bytes >= 0 && running.get()) {
                            if (paused.get()) {
                                // Pause condition
                                updateProgress(
                                    DownloadStatus.PAUSED,
                                    ((downloaded * 100) / total).toInt(),
                                    downloaded,
                                    total
                                )
                                currentDownloadPosition = downloaded
                                return
                            }

                            withContext(Dispatchers.IO) {
                                outputStream.write(buffer, 0, bytes)
                                outputStream.flush()
                            }

                            downloaded += bytes
                            bytes = inputStream.read(buffer)

                            // Update progress during the download
                            progress = ((downloaded * 100) / total).toInt()
                            updateProgress(DownloadStatus.DOWNLOADING, progress, downloaded, total)

                            // Update notification periodically
                            setForegroundAsync(createForegroundInfo("Downloaded: $progress%"))
                        }

                        if (!paused.get() && running.get()) {
                            // Download completed
                            currentDownloadPosition = 0L
                            resumePosition = 0L
                            updateProgress(DownloadStatus.COMPLETED, 100, downloaded, total)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            modelDownload.status = DownloadStatus.FAILED.name
            updateTable(modelDownload)
            throw e
        }
    }

    private suspend fun updateProgress(
        status: DownloadStatus,
        progress: Int = 0,
        downloaded: Long = 0,
        total: Long = 0
    ) {
        val data = createOutputData(
            when {
                isCanceling() -> DownloadStatus.CANCELED
                paused.get() -> DownloadStatus.PAUSED
                else -> status
            },
            progress, downloaded, total
        )
        setProgress(data)
    }

    private fun createOutputData(
        status: DownloadStatus,
        progress: Int = 0,
        downloaded: Long = 0,
        total: Long = 0
    ): Data {
        modelDownload.status = status.name
        modelDownload.progress = progress
        modelDownload.downloaded = downloaded
        modelDownload.total = total
        updateTable(modelDownload)
        return Data.Builder()
            .putString(DOWNLOAD_ID, modelDownload.id)
            .putInt(DOWNLOAD_PROGRESS, progress)
            .putLong(DOWNLOAD_CURRENT, downloaded)
            .putLong(DOWNLOAD_TOTAL, total)
            .putString(DOWNLOAD_STATUS, status.name)
            .build()
    }

    private fun createDir(modelDownload: ModelDownload) {
        File(modelDownload.directory).run {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private fun getPendingDownload() = downloadTable.getPendingDownload()
    private fun getPendingDownloads() = downloadTable.getPendingDownloads()
    fun updateTable(download: ModelDownload) = downloadTable.update(download)

    private fun logi(message: Any) {
        Log.e("ABENK : ", message.toString())
    }
}
