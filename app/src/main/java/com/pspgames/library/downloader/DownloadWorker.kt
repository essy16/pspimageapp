package com.pspgames.library.downloader

import android.app.Activity
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
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pspgames.library.*
import com.pspgames.library.ads.AdsUtils
import com.pspgames.library.database.DownloadTable
import com.pspgames.library.enums.DownloadStatus
import com.pspgames.library.interfaces.DownloadListener
import com.pspgames.library.model.ModelDownload
import com.pspgames.library.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.util.UUID
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
        private var mInterstitialAd: InterstitialAd? = null
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val DOWNLOAD_STATUS = "DOWNLOAD_STATUS"
        private var downloadListener: DownloadListener? = null
        private var modelDownload: ModelDownload = ModelDownload()
        private val running = AtomicBoolean(false)
        private val canceling = AtomicBoolean(false)
        private val paused = AtomicBoolean(false)
        private var currentDownloadPosition = 0L
        private var resumePosition = 0L
        val workRequest = OneTimeWorkRequest.Builder(DownloadWorker::class.java).build()
        val workId = workRequest.id

        private fun isRunning() = running.get()
        private fun isCanceling() = canceling.get()
        fun setListener(downloadListener: DownloadListener) {
            this.downloadListener = downloadListener
        }

        fun getAdvertisingId(context: Context) {

            GlobalScope.launch {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                Log.d("text", adInfo.id ?: "unknown")
            }
        }




        fun start(context: Context, observer: Observer<List<WorkInfo?>?>) {
            WorkManager.getInstance(context).enqueue(workRequest)


            if (!isRunning()) {
                canceling.set(false)
                removeObserver(context, observer)
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
//                val downloadRequest = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
//                    .setConstraints(constraints)
//                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    Constants.workerName,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
                addObserver(context, observer)
            }
        }

        fun addObserver(context: Context, observer: Observer<List<WorkInfo?>?>) {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkLiveData(Constants.workerName)
                .observeForever(observer)
        }

        fun observeWork(context: Context, workId: UUID, observer: Observer<WorkInfo>) {
            WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(workId)
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

        fun pause(context: Context, workId: UUID) {
            // Cancel the current work request
            WorkManager.getInstance(context).cancelWorkById(workId)

            // Save the current state (e.g., resumePosition) in a database or SharedPreferences
            val resumePosition = currentDownloadPosition // Assuming this is tracked in your worker
            saveResumePosition(context, workId, resumePosition)

            // Update the UI or notify the listener
            modelDownload.status = DownloadStatus.PAUSED.name
            downloadListener?.onPause(modelDownload)
        }

        private fun saveResumePosition(context: Context, workId: UUID, resumePosition: Long) {
            // Save the resume position in SharedPreferences or a database
            val sharedPrefs = context.getSharedPreferences("DownloadPrefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putLong(workId.toString(), resumePosition).apply()
        }

        fun resume(context: Context, workId: UUID, observer: Observer<List<WorkInfo?>?>) {
            // Retrieve the saved resume position
            val resumePosition = getResumePosition(context, workId)

            // Create input data with the resume position
            val inputData = Data.Builder()
                .putLong("resume_position", resumePosition)
                .putString("download_id", modelDownload.id) // Optional: Pass the download ID
                .build()

            // Create a new work request
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val downloadRequest = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            // Enqueue the new work request
            WorkManager.getInstance(context).enqueueUniqueWork(
                workId.toString(), // Use the Work ID as the unique work name
                ExistingWorkPolicy.REPLACE,
                downloadRequest
            )

            // Add an observer to track the work request
            addObserver(context, observer)
        }

        private fun getResumePosition(context: Context, workId: UUID): Long {
            // Retrieve the resume position from SharedPreferences or a database
            val sharedPrefs = context.getSharedPreferences("DownloadPrefs", Context.MODE_PRIVATE)
            return sharedPrefs.getLong(workId.toString(), 0L)
        }    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Get resume position from input data
            val resumePosition = inputData.getLong("resume_position", 0L)
            currentDownloadPosition = resumePosition

            // Perform the download
            performWork()
            Result.success()
        } catch (error: Exception) {
            Result.failure()
        }
    }

    fun createForegroundInfo(progress: String): ForegroundInfo {
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

                            // Update progress
                            val progress = ((downloaded * 100) / total).toInt()
                            broadcastProgress(
                                DownloadStatus.DOWNLOADING,
                                ((downloaded * 100) / total).toInt(),
                                downloaded,
                                total
                            )
                            // Update notification periodically
                            setForegroundAsync(createForegroundInfo("Downloaded: $progress%"))
                        }

                        if (!paused.get() && running.get()) {
                            // Download completed
                            currentDownloadPosition = 0L
                            resumePosition = 0L
                            updateProgress(DownloadStatus.COMPLETED, 100, downloaded, total)

                            // Show ad on main thread after download completes
                            Handler(Looper.getMainLooper()).post {
                                (downloadListener as? Context)?.let { context ->
                                    if (context is Activity && !context.isFinishing) {
                                        App.log("Valid Activity context found. Attempting to show ad.")

                                        val adRequest = AdRequest.Builder().build()

                                        InterstitialAd.load(context, AD_UNIT_ID, adRequest,
                                            object : InterstitialAdLoadCallback() {
                                                override fun onAdFailedToLoad(adError: LoadAdError) {
                                                    App.log("Ad failed to load: ${adError.message}")
                                                    mInterstitialAd = null
                                                }

                                                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                                    App.log("Ad loaded successfully")
                                                    mInterstitialAd = interstitialAd
                                                    downloadListener?.onComplete(modelDownload)


                                                    // Set full screen callback
                                                    mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                                                        override fun onAdDismissedFullScreenContent() {
                                                            App.log("Ad dismissed")
                                                            downloadListener?.onComplete(modelDownload)
                                                        }

                                                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                                            App.log("Ad failed to show")
                                                            downloadListener?.onComplete(modelDownload)
                                                        }
                                                    }

                                                    // Show the ad
                                                    mInterstitialAd?.show(context)
                                                    modelDownload.status = DownloadStatus.ADCOMPLETED.name

                                                }
                                            })
                                    } else {
                                        App.log("Invalid Activity context. Cannot show ad.")
                                        downloadListener?.onComplete(modelDownload)
                                    }
                                } ?: run {
                                    App.log("No context available. Cannot show ad.")
                                    downloadListener?.onComplete(modelDownload)
                                }
                            }
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

    private suspend fun broadcastProgress(
        status: DownloadStatus,
        progress: Int = 0,
        downloaded: Long = 0,
        total: Long = 0
    ) {
        // Create output data
        val data = createOutputData(status, progress, downloaded, total)

        // Set WorkManager progress
        setProgress(data)

        // Optional: Use a local broadcast or event bus to ensure UI gets update
        withContext(Dispatchers.Main) {
            downloadListener?.onProgressUpdate(
                modelDownload.copy(
                    status = status.name,
                    progress = progress,
                    downloaded = downloaded,
                    total = total
                )
            )
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