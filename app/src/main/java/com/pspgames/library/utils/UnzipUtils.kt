package com.pspgames.library.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.lingala.zip4j.ZipFile
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipException

object UnzipUtils {

    // Existing unzip functionality
    fun extract(context: Context, zipFile: File, outputDirectory: File, callback: UnzipCallback) {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir()
        }
        val archiveFileUri = Uri.fromFile(zipFile)
        val inputStream = context.contentResolver.openInputStream(archiveFileUri)
        val bufferedInputStream = BufferedInputStream(inputStream)

        extractPasswordProtectedZipOrRegularZip(context, bufferedInputStream, outputDirectory.absolutePath, callback)
    }

    private fun extractPasswordProtectedZipOrRegularZip(
        context: Context,
        bufferedInputStream: BufferedInputStream,
        outputDirectory: String,
        callback: UnzipCallback
    ) {
        callback.onStart()
        MainScope().launch {
            val tempFile = createTempFileFromInputStreamAsync(context, bufferedInputStream)
            zip4jExtractZipFile(context, tempFile, DocumentFile.fromFile(File(outputDirectory)), callback)
        }
    }

    private fun zip4jExtractZipFile(
        context: Context,
        tempFile: File,
        outputDirectory: DocumentFile?,
        callback: UnzipCallback
    ) {
        val zipFile = ZipFile(tempFile)
        zipFile.isRunInThread = true
        MainScope().launch(Dispatchers.IO) {
            try {
                val fileHeaders = zipFile.fileHeaders
                val totalEntries = fileHeaders.size
                var extractedEntries = 0

                for (header in fileHeaders) {
                    val relativePath = header.fileName
                    val pathParts = relativePath.split("/")
                    var currentDirectory = outputDirectory

                    // Create missing directories
                    for (part in pathParts.dropLast(1)) {
                        currentDirectory = currentDirectory?.findFile(part) ?: currentDirectory?.createDirectory(part)
                    }

                    // Extract the file if it's not a directory
                    if (!header.isDirectory) {
                        val filename = pathParts.last()
                        val mimeType = getMimeType(pathParts.last()) ?: "application/octet-stream"
                        val extension = pathParts.last().substringAfterLast(".", "")

                        // Handle specific MIME types
                        val finalMimeType = when {
                            extension.equals("iso", ignoreCase = true) -> "application/x-iso9660-image"
                            extension.equals("zip", ignoreCase = true) -> "application/zip"
                            else -> mimeType
                        }

                        val outputFile = currentDirectory?.createFile(finalMimeType, filename)
                        logging("Extracting file: ${outputFile?.uri}")

                        val bufferedOutputStream =
                            BufferedOutputStream(outputFile?.uri?.let { context.contentResolver.openOutputStream(it) })

                        zipFile.getInputStream(header).use { inputStream ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var bytesRead: Int
                            var totalBytesRead = 0L
                            val fileSize = header.uncompressedSize

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                bufferedOutputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                val progress = (totalBytesRead * 100 / fileSize).toInt()
                                MainScope().launch(Dispatchers.Main) {
                                    callback.onProgress(progress)
                                }
                            }
                            bufferedOutputStream.close()
                        }
                    }

                    extractedEntries++

                    val progress = (extractedEntries * 100 / totalEntries)
                    MainScope().launch(Dispatchers.Main) {
                        callback.onProgress(progress)
                    }
                }
                tempFile.delete()
                logging("Temporary file deleted: ${tempFile.absolutePath}")

            } catch (e: ZipException) {
                MainScope().launch(Dispatchers.Main) {
                    callback.onError(e)
                }
            } finally {
                MainScope().launch(Dispatchers.Main) {
                    callback.onComplete()
                }
            }
        }
    }

    private suspend fun createTempFileFromInputStreamAsync(context: Context, inputStream: InputStream): File =
        withContext(Dispatchers.IO) {
            val tempFile = File.createTempFile("temp_", "", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var count: Int
                while (inputStream.read(buffer).also { count = it } != -1) {
                    outputStream.write(buffer, 0, count)
                }
            }
            return@withContext tempFile
        }

    // New functionality: Download and unzip
    suspend fun downloadAndUnzip(
        context: Context,
        url: String,
        outputDirectory: File,
        callback: UnzipCallback,
        chunkSize: Long = 5 * 1024 * 1024, // 5 MB per chunk
        maxConcurrentDownloads: Int = 5
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Step 1: Download the file in chunks
            val tempFile = File(context.cacheDir, "temp_download.zip")
            val downloader = ChunkedDownloader(context)
            val downloadSuccess = downloader.downloadFile(
                url,
                tempFile,
                chunkSize,
                maxConcurrentDownloads
            ) { progress ->
                // Forward download progress to the callback
                MainScope().launch(Dispatchers.Main) {
                    callback.onProgress(progress / 2) // Download is half the work
                }
            }

            if (!downloadSuccess) {
                throw IOException("Failed to download the file")
            }

            // Step 2: Unzip the downloaded file
            extract(context, tempFile, outputDirectory, object : UnzipCallback {
                override fun onStart() {
                    callback.onStart()
                }

                override fun onComplete() {
                    tempFile.delete() // Clean up the downloaded file
                    callback.onComplete()
                }

                override fun onProgress(progress: Int) {
                    // Adjust progress to account for download + unzip
                    callback.onProgress(50 + progress / 2)
                }

                override fun onError(e: Exception) {
                    tempFile.delete() // Clean up the downloaded file
                    callback.onError(e)
                }
            })

            true
        } catch (e: Exception) {
            Log.e("UnzipUtils", "Error during download and unzip: ${e.message}")
            callback.onError(e)
            false
        }
    }

    // Helper function for logging
    private fun logging(message: String) {
        Log.d("UnzipUtils", message)
    }

    // Existing MIME type utility
    fun getMimeType(string: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(string)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    // Existing callback interface
    interface UnzipCallback {
        fun onStart()
        fun onComplete()
        fun onProgress(progress: Int)
        fun onError(e: Exception)
    }

    // ChunkedDownloader class (moved inside UnzipUtils)
    private class ChunkedDownloader(private val context: Context) {
        private val client = OkHttpClient()
        private val mutex = Mutex()

        suspend fun downloadFile(
            url: String,
            destination: File,
            chunkSize: Long = 5 * 1024 * 1024, // 5 MB per chunk
            maxConcurrentDownloads: Int = 5,
            onProgress: (progress: Int) -> Unit
        ): Boolean = withContext(Dispatchers.IO) {
            try {
                // Get the total file size
                val totalSize = getFileSize(url)
                if (totalSize <= 0) throw IOException("Invalid file size")

                Log.d("ChunkedDownloader", "Total file size: $totalSize bytes")

                // Create a temporary directory to store chunks
                val tempDir = File(context.cacheDir, "temp_chunks")
                if (tempDir.exists()) tempDir.deleteRecursively()
                tempDir.mkdirs()

                Log.d("ChunkedDownloader", "Temporary directory created: ${tempDir.absolutePath}")

                // Calculate the number of chunks
                val numberOfChunks = (totalSize + chunkSize - 1) / chunkSize

                Log.d("ChunkedDownloader", "Number of chunks: $numberOfChunks")

                // Tracking progress
                val downloadedChunks = AtomicInteger(0)

                // Create a coroutine scope with limited parallelism
                val dispatcher = Dispatchers.IO.limitedParallelism(maxConcurrentDownloads)

                // Download chunks concurrently
                val jobs = (0 until numberOfChunks).map { chunkIndex ->
                    async(dispatcher) {
                        val chunkFile = File(tempDir, "chunk_$chunkIndex.tmp")
                        Log.d("ChunkedDownloader", "Downloading chunk $chunkIndex to ${chunkFile.absolutePath}")
                        downloadChunk(
                            url,
                            chunkFile,
                            chunkIndex * chunkSize,
                            minOf((chunkIndex + 1) * chunkSize - 1, totalSize - 1)
                        ) {
                            // Update progress
                            val completedChunks = downloadedChunks.incrementAndGet()
                            val progress = ((completedChunks.toFloat() / numberOfChunks) * 100).toInt()
                            onProgress(minOf(progress, 100))
                            Log.d("ChunkedDownloader", "Chunk $chunkIndex completed. Progress: $progress%")
                        }
                    }
                }

                // Wait for all chunks to complete
                jobs.awaitAll()

                Log.d("ChunkedDownloader", "All chunks downloaded. Combining chunks...")

                // Combine chunks into the final file
                combineChunks(tempDir, destination, numberOfChunks.toInt())

                Log.d("ChunkedDownloader", "Chunks combined into final file: ${destination.absolutePath}")

                // Clean up temporary files
                tempDir.deleteRecursively()

                Log.d("ChunkedDownloader", "Temporary files cleaned up.")

                true
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ChunkedDownloader", "Error during download: ${e.message}")
                false
            }
        }

        private suspend fun getFileSize(url: String): Long = withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).head().build()
            val response = client.newCall(request).execute()

            response.use {
                if (it.isSuccessful) {
                    val contentLength = it.body?.contentLength() ?: -1
                    Log.d("ChunkedDownloader", "File size retrieved: $contentLength bytes")
                    contentLength
                } else {
                    Log.e("ChunkedDownloader", "Failed to retrieve file size: ${it.code}")
                    -1
                }
            }
        }

        private suspend fun downloadChunk(
            url: String,
            chunkFile: File,
            start: Long,
            end: Long,
            onChunkComplete: () -> Unit
        ) = withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .header("Range", "bytes=$start-$end")
                .build()

            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    response.use { resp ->
                        resp.body?.byteStream()?.use { inputStream ->
                            chunkFile.outputStream().use { outputStream ->
                                val buffer = ByteArray(8192)
                                var bytesRead: Int

                                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                    outputStream.write(buffer, 0, bytesRead)
                                }
                            }
                        }
                    }

                    Log.d("ChunkedDownloader", "Chunk downloaded: ${chunkFile.absolutePath} (bytes $start-$end)")

                    // Chunk download complete
                    onChunkComplete()
                } else {
                    throw IOException("Failed to download chunk: ${response.code}")
                }
            } catch (e: Exception) {
                throw IOException("Chunk download error: ${e.message}")
            }
        }

        private suspend fun combineChunks(tempDir: File, destination: File, numberOfChunks: Int) =
            withContext(Dispatchers.IO) {
                destination.outputStream().use { outputStream ->
                    for (i in 0 until numberOfChunks) {
                        val chunkFile = File(tempDir, "chunk_$i.tmp")
                        Log.d("ChunkedDownloader", "Combining chunk $i: ${chunkFile.absolutePath}")
                        chunkFile.inputStream().use { inputStream ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                }
            }
    }
}