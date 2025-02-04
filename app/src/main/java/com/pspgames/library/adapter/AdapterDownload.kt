package com.pspgames.library.adapter


import ChunkedDownloader
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.pspgames.library.App
import com.pspgames.library.R
import com.pspgames.library.activity.ActivityDownload
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.database.DownloadTable
import com.pspgames.library.databinding.ItemDownloadBinding
import com.pspgames.library.dialog.DialogDelete
import com.pspgames.library.dialog.DialogOpenIso
import com.pspgames.library.dialog.DialogRename
import com.pspgames.library.downloader.NotificationUtils
import com.pspgames.library.enums.DownloadStatus
import com.pspgames.library.model.ModelDownload
import com.pspgames.library.utils.UnzipUtils
import com.pspgames.library.utils.Utils
import com.pspgames.library.utils.hide
import com.pspgames.library.utils.show
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pspgames.library.ads.AdsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class AdapterDownload(private val downloadTable: DownloadTable) : BaseRVAdapter<ModelDownload, ItemDownloadBinding>() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemDownloadBinding> {
        return BaseViewHolder(parent.toBinding())
    }

    @SuppressLint("SetTextI18n", "FileEndsWithExt")
    override fun convert(binding: ItemDownloadBinding, item: ModelDownload, position: Int) {
        binding.buttonCard.isEnabled = true
        binding.fileName.text = item.title
        binding.progress.max = 100f
        binding.progress.progress = item.progress.toFloat()
        binding.percent.text = "${item.progress}%"
        binding.size.text = Utils.getHumanReadableString(item.downloaded) + " / " + Utils.getHumanReadableString(item.total)
        Glide.with(binding.thumbnail)
            .load(item.thumbnail)
            .placeholder(R.drawable.placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.thumbnail)

        binding.options.setOnClickListener {
            openOptionMenu(context, binding, item, position)
        }
        binding.buttonRemove.setOnClickListener { removeWithDialog(context, item) }
        resetLayout()

        when (item.status) {
            DownloadStatus.UNZIPPED.name -> {
                progressComplete()
                binding.size.text = Utils.getHumanReadableString(item.total)
                binding.progress.progress = 100f
                binding.percent.text = "100%"
                binding.buttonRemove.show()
                binding.buttonText.setText(R.string.open)
                binding.buttonCard.setOnClickListener {
                    openImageFile(context, item)
                }
            }

            DownloadStatus.COMPLETED.name -> {
                progressComplete()
                binding.size.text = Utils.getHumanReadableString(item.total)
                binding.progress.progress = 100f
                binding.percent.text = "100%"
                binding.buttonRemove.show()
                binding.buttonCard.isEnabled = true

                // Show interstitial ad after download is completed
                val activity = (recyclerView.context as? Activity)
                activity?.let { activityContext ->
                    AdsUtils.showInterstitial(activityContext) {
                        // This code runs after the ad is shown or dismissed
                        if (item.filename.endsWith(".zip", true)) {
                            downloadZipFile(item, position) // Call downloadZipFile here
                        } else {
                            scanFile(item.createFile())
                            binding.buttonText.setText(R.string.open)
                            binding.buttonCard.setOnClickListener {
                                openImageFile(context, item)
                            }
                        }
                    }
                } ?: run {
                    // If we couldn't get activity context, proceed without showing the ad
                    if (item.filename.endsWith(".zip", true)) {
                        downloadZipFile(item, position) // Call downloadZipFile here
                    } else {
                        scanFile(item.createFile())
                        binding.buttonText.setText(R.string.open)
                        binding.buttonCard.setOnClickListener {
                            openImageFile(context, item)
                        }
                        // Add this dialog to prompt user to open file
                        AlertDialog.Builder(context)
                            .setTitle("Download Complete")
                            .setMessage("Would you like to open this file now?")
                            .setPositiveButton("Open") { dialog, _ ->
                                openImageFile(context, item)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Later") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            }

            DownloadStatus.ADCOMPLETED.name -> {
                binding.buttonRemove.show()
                binding.buttonText.setText(R.string.cancel)
                binding.buttonRemoveText.setText(R.string.delete)
                binding.buttonCard.setOnClickListener {
                    cancel(item)
                }
                binding.buttonRemove.setOnClickListener {
                    removeWithDialog(context, item)
                }
            }

            DownloadStatus.FAILED.name -> {
                binding.buttonRemove.show()
                binding.buttonText.setText(R.string.retry)
                binding.buttonCard.setOnClickListener {
                    retry(item)
                }
                resume(item)
            }

            DownloadStatus.CANCELED.name -> {
                binding.buttonRemove.show()
                binding.buttonText.setText(R.string.retry)
                binding.buttonCard.setOnClickListener {
                    retry(item)
                }
            }

            DownloadStatus.DOWNLOADING.name -> {
                binding.buttonRemove.show()
                binding.buttonText.setText(R.string.cancel)
                binding.buttonRemoveText.setText(R.string.pause)
                binding.buttonCard.setOnClickListener {
                    cancel(item)
                }
                binding.buttonRemove.setOnClickListener {
                    pause(item)
                    item.status = DownloadStatus.PAUSED.name
                }
            }

            DownloadStatus.PAUSED.name -> {
                binding.buttonRemoveText.text = "RESUME"
                binding.buttonText.setText(R.string.cancel)
                binding.buttonCard.setOnClickListener {
                    cancel(item)
                }
                binding.buttonRemove.setOnClickListener {
                    resume(item)
                }
            }

            DownloadStatus.ENQUEUE.name -> {
                showLoading()
                binding.buttonText.setText(R.string.queued)
                binding.buttonRemove.hide()
                binding.buttonCard.setOnClickListener(null)
            }
        }
    }

    private fun downloadZipFile(item: ModelDownload, position: Int) {
        coroutineScope.launch {
            val zipFile = File(item.directory, item.filename)
            val notificationId = item.id.toInt()

            // Update status to downloading
            item.status = DownloadStatus.DOWNLOADING.name
            downloadTable.update(item)
            notifyItemChanged(position)

            try {
                // Initial notification
                NotificationUtils.sendStatusNotification(
                    "Starting download: ${item.filename}",
                    context,
                    notificationId
                )

                // Use UnzipUtils.downloadAndUnzip for downloading and unzipping
                val success = UnzipUtils.downloadAndUnzip(
                    context = context,
                    url = item.url,
                    outputDirectory = File(Utils.getDownloadPath(context)),
                    callback = object : UnzipUtils.UnzipCallback {
                        override fun onStart() {
                            // Update status to downloading
                            item.status = DownloadStatus.DOWNLOADING.name
                            downloadTable.update(item)
                            notifyItemChanged(position)
                        }

                        override fun onComplete() {
                            // Update status and UI
                            item.status = DownloadStatus.UNZIPPED.name
                            item.directory = Utils.getDownloadPath(context)
                            item.progress = 100
                            downloadTable.update(item)
                            notifyItemChanged(position)

                            // Show success notification
                            NotificationUtils.sendUnzipSuccess(
                                "${item.filename} downloaded and extracted successfully",
                                context,
                                notificationId
                            )

                            // Clean up zip file
                            zipFile.delete()

                            // Scan media files
                            scanFile(File(item.directory, item.filename))
                        }

                        override fun onProgress(progress: Int) {
                            // Update download progress in UI and model
                            item.progress = progress
                            item.downloaded = (progress.toFloat() / 100 * item.total).toLong()
                            downloadTable.update(item)

                            // Update UI
                            binding.progress.progress = progress.toFloat()
                            binding.percent.text = "$progress%"
                            binding.size.text = "${Utils.getHumanReadableString(item.downloaded)} / ${Utils.getHumanReadableString(item.total)}"

                            // Update notification every 5%
                            if (progress % 5 == 0) {
                                NotificationUtils.sendStatusNotification(
                                    "Downloading ${item.filename}: $progress%",
                                    context,
                                    notificationId
                                )
                            }
                        }

                        override fun onError(e: Exception) {
                            // Update status and UI for error
                            item.status = DownloadStatus.FAILED.name
                            downloadTable.update(item)
                            notifyItemChanged(position)

                            // Show error notification
                            NotificationUtils.sendStatusNotification(
                                "Error downloading ${item.filename}: ${e.message}",
                                context,
                                notificationId
                            )

                            // Clean up any partial files
                            zipFile.delete()
                        }
                    },
                    chunkSize = 5 * 1024 * 1024, // 5MB chunks
                    maxConcurrentDownloads = 3
                )

                if (!success) {
                    // Handle download failure
                    item.status = DownloadStatus.FAILED.name
                    downloadTable.update(item)
                    notifyItemChanged(position)

                    NotificationUtils.sendStatusNotification(
                        "Failed to download ${item.filename}",
                        context,
                        notificationId
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()

                // Update status and UI for error
                item.status = DownloadStatus.FAILED.name
                downloadTable.update(item)
                notifyItemChanged(position)

                // Show error notification
                NotificationUtils.sendStatusNotification(
                    "Error downloading ${item.filename}: ${e.message}",
                    context,
                    notificationId
                )

                // Clean up any partial files
                zipFile.delete()
            }
        }
    }


    private fun openOptionMenu(context: Context, binding: ItemDownloadBinding, item: ModelDownload, position: Int) {
        val popupMenu = PopupMenu(context, binding.options)
        popupMenu.inflate(R.menu.download_menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.rename -> {
                    DialogRename(context, item) {
                        item.filename = it
                        downloadTable.update(item)
                        notifyItemChanged(position)
                        scanFile(item.createFile())
                    }.show()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun resetLayout() {
        binding.size.show()
        binding.progress.progressColor = ContextCompat.getColor(context, R.color.accent_color)
        binding.progressLoading.hide()
        binding.progress.show()
    }

    private fun progressComplete() {
        binding.progress.progressColor = ContextCompat.getColor(context, R.color.success)
    }

    private fun showLoading() {
        binding.progressLoading.show()
        binding.progress.hide()
    }

    private fun scanFile(file: File) {
        MediaScannerConnection.scanFile(
            context, arrayOf(file.absolutePath),
            null
        ) { _, _ -> }
    }


    private fun openImageFile(context: Context, item: ModelDownload) {
        val file = File(item.directory, item.filename)
        App.log(file.absolutePath)
        val uri =
            FileProvider.getUriForFile(context, context.packageName + ".provider", file)
        var mimeType = if (Utils.isMP4(item.filename)) "video/*" else "image/*"
        mimeType = if (Utils.isGif(item.filename)) "image/gif" else mimeType
        mimeType = if (Utils.isIso(item.filename)) "application/x-iso9660-image" else mimeType
        try {
            Intent(Intent.ACTION_VIEW).run {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(this)
            }
        } catch (e: Exception) {
            DialogOpenIso(context, file.absolutePath).show()
        }
    }

    private fun cancel(modelDownload: ModelDownload) {
        modelDownload.status = DownloadStatus.CANCELED.name
        downloadTable.update(modelDownload)
        (context as ActivityDownload).cancel()
    }
    private fun pause(modelDownload: ModelDownload){
        modelDownload.status = DownloadStatus.PAUSED.name
        downloadTable.update(modelDownload)
        (context as ActivityDownload).pause()
    }
    private fun resume(modelDownload: ModelDownload){
        modelDownload.status = DownloadStatus.RESUMED.name
        downloadTable.update(modelDownload)
        (context as ActivityDownload).resume()
    }

    private fun retry(modelDownload: ModelDownload) {
        modelDownload.status = DownloadStatus.ENQUEUE.name
        downloadTable.update(modelDownload)
        val position = data.indexOf(modelDownload)
        data[position] = modelDownload
        notifyItemChanged(position)
        (context as ActivityDownload).retry()
    }

    private fun removeWithDialog(context: Context, modelDownload: ModelDownload) {
        DialogDelete(context) {
            remove(modelDownload)
        }.show()
    }

    private fun remove(modelDownload: ModelDownload) {
        try {
            val file = File(modelDownload.directory, modelDownload.filename)
            if (Utils.isZipFile(modelDownload.url)) {
                deleteFilesInDirectory(Utils.getDownloadPath(context), file.nameWithoutExtension)
            } else {
                if (file.exists()) {
                    file.delete()
                }
            }

            downloadTable.delete(modelDownload.id)
            for (position in 0 until data.size) {
                if (modelDownload.id == data[position].id) {
                    data.removeAt(position)
                    notifyItemRemoved(position)
                    return
                }
            }
        } catch (e: Exception) {
            App.toast(R.string.remove_failed)
        }
    }

    private fun deleteFilesInDirectory(dir: String, filenameWithoutExtensions: String) {
        File(dir).run {
            if (isDirectory) {
                val listFiles = listFiles()
                listFiles?.let {
                    it.forEach { file ->
                        if (file.name.contains(filenameWithoutExtensions)) file.delete()
                    }
                }
            }
        }
    }

    fun cancelAllDownloads() {
        coroutineScope.cancel()
    }

    fun update(downloadData: ModelDownload) {
        for (position in 0 until data.size) {
            val modelDownload = data[position]
            if (downloadData.url == data[position].url) {
                when (downloadData.status) {
                    DownloadStatus.REMOVED.name -> {
                        remove(modelDownload)
                    }

                    else -> {
                        data[position].progress = downloadData.progress
                        data[position].downloaded = downloadData.downloaded
                        data[position].total = downloadData.total
                        data[position].status = downloadData.status
                        notifyItemChanged(position)
                    }
                }
                return
            }
        }
    }
}

