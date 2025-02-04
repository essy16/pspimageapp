import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicInteger

class ChunkedDownloader(private val context: Context) {
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