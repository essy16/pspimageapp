package com.pspgames.library.utils



import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipException



object UnzipUtils {
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



    fun getMimeType(string: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(string)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }



    private fun zip4jExtractZipFile(context: Context, tempFile: File, outputDirectory: DocumentFile?, callback: UnzipCallback) {
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



    private suspend fun createTempFileFromInputStreamAsync(context: Context, inputStream: InputStream): File = withContext(Dispatchers.IO) {
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



    interface UnzipCallback {
        fun onStart()
        fun onComplete()
        fun onProgress(progress: Int)
        fun onError(e: Exception)
    }
}