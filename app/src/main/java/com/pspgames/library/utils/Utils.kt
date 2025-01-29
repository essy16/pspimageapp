package com.pspgames.library.utils


import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.MimeTypeMap
import coil.imageLoader
import com.pspgames.library.App
import com.pspgames.library.Config
import com.pspgames.library.R
import com.pspgames.library.base.handler
import com.pspgames.library.databinding.ItemDetailBinding
import com.pspgames.library.dialog.DialogProgress
import com.pspgames.library.services.GifWallpaperService
import com.downloader.PRDownloader
import com.downloader.OnDownloadListener
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.CharacterIterator
import java.text.DecimalFormat
import java.text.StringCharacterIterator
import java.util.UUID


object Utils {
    fun getMimeType(context: Context, uri: Uri): String {
        val cR = context.contentResolver
        val mime = MimeTypeMap.getSingleton()
        var type = mime.getExtensionFromMimeType(cR.getType(uri))
        if (type == null) {
            type = "*/*"
        }
        return type
    }
    fun startAct(context: Context, clazz: Class<*>){
        val intent = Intent(context, clazz)
        context.startActivity(intent)
    }

    fun startAct(context: Context, clazz: Class<*>, bundle: Bundle){
        val intent = Intent(context, clazz)
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    fun getDownloadSpeedString(context: Context, downloadedBytesPerSecond: Long): String {
        if (downloadedBytesPerSecond < 0) {
            return ""
        }
        val kb = downloadedBytesPerSecond.toDouble() / 1000.0
        val mb = kb / 1000.0
        val decimalFormat = DecimalFormat(".##")
        return if (mb >= 1) {
            context.getString(R.string.download_speed_mb, decimalFormat.format(mb))
        } else if (kb >= 1) {
            context.getString(R.string.download_speed_kb, decimalFormat.format(kb))
        } else {
            context.getString(R.string.download_speed_bytes, downloadedBytesPerSecond)
        }
    }

    fun getHumanReadableString(size: Long): String {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        if (size < sizeMb) return df.format((size / sizeKb).toDouble()) + " KB"
        else if (size < sizeGb) return df.format((size / sizeMb).toDouble()) + " MB"
        else if (size < sizeTerra) return df.format((size / sizeGb).toDouble()) + " GB"
        return ""
    }


    fun generateThumbnail(string: String, size: Int): String {
        val thumbnail = if(string.startsWith("http")){
            string
        } else {
            Config.SERVER_URL + "/" + string
        }
        return if(Config.ENABLE_THUMBNAIL_INSTEAD_ORIGINAL_IMAGE){
            if(!thumbnail.endsWith("gif") && !thumbnail.endsWith("mp4")){
                Config.SERVER_URL + "/thumbnail?size=$size&url=" + thumbnail
            } else {
                thumbnail
            }
        } else {
            thumbnail
        }
    }

    fun getFileNameFromIso(url: String) = "${getRandomString(10)}.iso"

    fun getFileNameFromUrl(url: String) : String {
        if(isMP4(url)){
            if(url.endsWith(".mp4", true)){
                val extension = url.substring(url.lastIndexOf("."))
                return "${getRandomString(10)}$extension"
            } else {
                return "${getRandomString(10)}.mp4"
            }
        } else {
            if(url.endsWith(".png", true) || url.endsWith(".jpg", true) || url.endsWith("jpeg", true)){
                val extension = url.substring(url.lastIndexOf("."))
                return "${getRandomString(10)}$extension"
            } else {
                return "${getRandomString(10)}.png"
            }
        }
    }

    fun isZipFile(url: String) : Boolean {
        val extension = url.substring(url.lastIndexOf("."))
        return extension.contains("zip")
    }

    fun isIso(url: String) : Boolean {
        val extension = url.substring(url.lastIndexOf("."))
        return extension.contains("iso")
    }

    fun isMP4(url: String) : Boolean {
        val extension = url.substring(url.lastIndexOf("."))
        return extension.contains("mp4")
    }

    fun isGif(url: String) : Boolean {
        val extension = url.substring(url.lastIndexOf("."))
        return extension.contains("gif")
    }

    fun isImage(url: String) : Boolean {
        val extension = url.substring(url.lastIndexOf("."))
        return if(extension.contains("png")){
            true
        } else if(extension.contains("jpg")){
            true
        } else if(extension.contains("jpeg")){
            true
        }  else {
            false
        }
    }

    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'.rangeTo('Z')) + ('a'.rangeTo('z')) + ('0'.rangeTo('9'))
        return (1.rangeTo(length))
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun getRealUrl(string: String): String {
        return if(string.contains("http")){
            string
        } else {
            Config.SERVER_URL + "/" + string
        }
    }

    fun getBytesFromFile(file: File): ByteArray {
        val length: Long = file.length()
        if (length > Int.MAX_VALUE) {
            throw IOException("File is too large!")
        }
        val bytes = ByteArray(length.toInt())
        var offset = 0
        var numRead = 0
        val `is`: InputStream = FileInputStream(file)
        try {
            while (offset < bytes.size
                && `is`.read(bytes, offset, bytes.size - offset).also { numRead = it } >= 0
            ) {
                offset += numRead
            }
        } finally {
            `is`.close()
        }
        if (offset < bytes.size) {
            throw IOException("Could not completely read file " + file.name)
        }
        return bytes
    }

    fun getDirPath(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .absolutePath + "/" + context.getString(
                com.pspgames.library.R.string.app_name
            )
        } else {
            Environment.getExternalStorageDirectory().absolutePath + "/" + context.getString(
                com.pspgames.library.R.string.app_name
            )
        }
    }


    fun getZipPath(context: Context): String {
        return File(context.externalCacheDir, context.getString(
            R.string.app_name
        )).absolutePath
    }

    fun getExtension(input: String) : String{
        return input.substring(input.lastIndexOf("."))
    }

    fun getDownloadPath(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .absolutePath ,context.getString(
                R.string.app_name
            )).absolutePath
        } else {
            File(Environment.getExternalStorageDirectory().absolutePath , context.getString(
                R.string.app_name
            )).absolutePath
        }
    }


    fun downloadToInternal(url: String, context: Context, binding: ItemDetailBinding, path: String, scanning: Boolean? = true, toast: Boolean? = true, onSuccess: ((String) -> Unit)? = null){
        val extension = url.substring(url.lastIndexOf("."))
        if(url.endsWith("mp4")){
            downloadVideoToCache(url, context, binding){
                downloadFile(context, getBytesFromFile(File(it)), "${UUID.randomUUID()}$extension",
                    File(getDirPath(context)),
                    onSuccess = {
                        App.toast(context.getString(com.pspgames.library.R.string.download_success))
                    },
                    onError = {
                        App.toast(context.getString(com.pspgames.library.R.string.download_failed))
                    }
                )
            }
        } else {
            val fileName = "${UUID.randomUUID()}$extension"
            val downloadId = PRDownloader.download(url, path,  fileName)
                .build()
                .setOnProgressListener {
                    binding.progressBar.max = 100f
                    binding.layoutProgress.visibility = View.VISIBLE
                }
                .setOnCancelListener {
                    if(toast!!){
                        App.toast(context.getString(com.pspgames.library.R.string.download_cancel))
                    }
                    binding.buttonPlay.visibility = View.VISIBLE
                    binding.layoutProgress.visibility = View.GONE
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        binding.layoutProgress.visibility = View.GONE
                        onSuccess?.invoke(File(path, fileName).absolutePath)
                        if(toast!!) {
                            App.toast(context.getString(com.pspgames.library.R.string.download_success))
                        }
                        if(scanning!!){
                            MediaScannerConnection.scanFile(
                                context, arrayOf(File(path).absolutePath),
                                null
                            ) { _, _ -> }
                        }
                    }
                    override fun onError(error: com.downloader.Error?) {
                        binding.layoutProgress.visibility = View.GONE
                        App.toast(context.getString(com.pspgames.library.R.string.download_failed))
                    }
                })
            binding.buttonCancelDownload.setOnClickListener {
                PRDownloader.cancel(downloadId)
            }
        }
    }

    private fun downloadFile(context: Context, bytes: ByteArray, imgName: String, dir: File, onSuccess: () -> Unit, onError: () -> Unit) {
        try {
            var success = true
            if (!dir.exists()) {
                success = dir.mkdirs()
            }
            if (success) {
                val imageFile = File(dir, imgName)
                val fileWriter = FileOutputStream(imageFile)
                fileWriter.write(bytes)
                fileWriter.flush()
                fileWriter.close()
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val file = File(imageFile.absolutePath)
                val contentUri: Uri = Uri.fromFile(file)
                mediaScanIntent.data = contentUri
                context.sendBroadcast(mediaScanIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onError.invoke()
        } finally {
            onSuccess.invoke()
        }
    }

    fun downloadVideoForSet(url: String, context: Context, binding: ItemDetailBinding, callback: () -> Unit){
        val dirPath = File(context.filesDir , "VIDEO").absolutePath
        val fileName = url.substring(url.lastIndexOf('/') + 1)
        val videoFile = File(dirPath, fileName)
        if(!videoFile.exists()){
            binding.progressBar.max = 100f
            binding.buttonPlay.visibility = View.GONE
            binding.layoutProgress.visibility = View.VISIBLE
            val downloadId = PRDownloader.download(url, dirPath,  "pspgames.mp4")
                .build()
                .setOnProgressListener {
                    handler {
                        val progress = (it.currentBytes * 100) / it.totalBytes
                        binding.progressBar.progress = progress.toFloat()
                    }
                }
                .setOnCancelListener {
                    App.toast(context.getString(com.pspgames.library.R.string.download_cancel))
                    binding.buttonPlay.visibility = View.VISIBLE
                    binding.layoutProgress.visibility = View.GONE
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        binding.buttonPlay.visibility = View.VISIBLE
                        binding.layoutProgress.visibility = View.GONE
                        callback.invoke()
                    }
                    override fun onError(error: com.downloader.Error?) {
                        App.toast(context.getString(com.pspgames.library.R.string.download_failed))
                        binding.buttonPlay.visibility = View.VISIBLE
                        binding.layoutProgress.visibility = View.GONE
                    }
                })
            binding.buttonCancelDownload.setOnClickListener {
                PRDownloader.cancel(downloadId)
            }
        } else {
            downloadFile(context, getBytesFromFile(videoFile), "pspgames.mp4",
                File(dirPath),
                onSuccess = {
                    callback.invoke()
                },
                onError = {
                    App.toast(context.getString(com.pspgames.library.R.string.download_failed))
                }
            )
        }
    }

    fun downloadVideoToCache(url: String, context: Context, binding: ItemDetailBinding, callback: (String) -> Unit){
        val dirPath = File(context.filesDir , "VIDEO").absolutePath
        //val fileName = base64encode(url)
        val fileName = url.substring(url.lastIndexOf('/') + 1)
        val videoFile = File(dirPath, fileName)
        if(!videoFile.exists()){
            binding.progressBar.max = 100f
            binding.buttonPlay.visibility = View.GONE
            binding.layoutProgress.visibility = View.VISIBLE
            val downloadId = PRDownloader.download(url.replace("localhost", "192.168.1.51"), dirPath,  fileName)
                .build()
                .setOnProgressListener {
                    handler {
                        val progress = (it.currentBytes * 100) / it.totalBytes
                        binding.progressBar.progress = progress.toFloat()
                    }
                }
                .setOnCancelListener {
                    App.toast(context.getString(com.pspgames.library.R.string.download_cancel))
                    binding.buttonPlay.visibility = View.VISIBLE
                    binding.layoutProgress.visibility = View.GONE
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        binding.buttonPlay.visibility = View.VISIBLE
                        binding.layoutProgress.visibility = View.GONE
                        callback.invoke(videoFile.absolutePath)
                    }
                    override fun onError(error: com.downloader.Error?) {
                        App.toast(context.getString(com.pspgames.library.R.string.download_failed))
                        binding.buttonPlay.visibility = View.VISIBLE
                        binding.layoutProgress.visibility = View.GONE
                    }
                })
            binding.buttonCancelDownload.setOnClickListener {
                PRDownloader.cancel(downloadId)
            }
        } else {
            callback.invoke(videoFile.absolutePath)
        }
    }

    fun setWallpaperAsGif(url: String, context: Context){
        val dialogProgress = DialogProgress(context)
        dialogProgress.show()
        PRDownloader.download(url, context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath, "image.gif")
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    try {
                        GifWallpaperService.start(context)
                        dialogProgress.dismiss()
                    } catch (e: Exception) {
                        App.toast(context.getString(com.pspgames.library.R.string.something_error))
                        dialogProgress.dismiss()
                    }
                }
                override fun onError(error: com.downloader.Error?) {
                    App.toast(context.getString(com.pspgames.library.R.string.something_error))
                }
            })
    }

    fun getImageCacheSize(context: Context): String {
        val size =  File(context.cacheDir, "image_cache")
            .walkTopDown()
            .map { it.length() }
            .sum()
        return humanReadableByteCountSI(size)
    }

    private fun humanReadableByteCountSI(bytes: Long): String {
        var newBytes = bytes
        if (-1000 < newBytes && newBytes < 1000) {
            return "$newBytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        while (newBytes <= -999950 || newBytes >= 999950) {
            newBytes /= 1000
            ci.next()
        }
        return String.format("%.1f %cB", newBytes / 1000.0, ci.current())
    }

    fun clearImageCache(context: Context) {
        try {
            val imageLoader = context.imageLoader
            imageLoader.memoryCache?.clear()
            val file = File(context.cacheDir, "image_cache")
            file.deleteRecursively()
        } catch (e: Exception) {
            App.log(e)
        }
    }
    
    fun base64encode(string: String): String {
        return android.util.Base64.encodeToString(string.toByteArray(charset("UTF-8")), android.util.Base64.DEFAULT)
    }
}