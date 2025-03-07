import android.content.Context
import com.pspgames.library.downloader.DownloadManager
import com.pspgames.library.model.ModelDownload
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.utils.Utils

class ServerSelectionManager(
    private val context: Context,
    private val model: ModelLatest.Data,
    private val servers: List<String>
) {
    private val downloadManager by lazy { DownloadManager(context) }

    private var currentServerIndex = 0
    private var maxRetries = 3
    private var currentRetries = 0
    private var totalAttempts = 0
    private val maxTotalAttempts = servers.size * maxRetries  // Prevent infinite loops

    fun getCurrentServer(): String = servers[currentServerIndex]

    fun shouldTryNextServer(): Boolean = currentServerIndex < servers.size - 1

    fun moveToNextServer() {
        currentServerIndex++
        currentRetries = 0
    }

    fun incrementRetries() {
        currentRetries++
        totalAttempts++
    }

    fun hasExceededRetries(): Boolean = currentRetries >= maxRetries

    fun downloadFile(onSuccess: (ModelDownload) -> Unit, onFailure: (String) -> Unit) {
        if (totalAttempts >= maxTotalAttempts) {
            onFailure("All download attempts failed. Please try again later.")
            return
        }

        val currentServer = getCurrentServer()
        val filename = if (Utils.isIso(currentServer)) "${model.title}.iso" else "${model.title}.zip"

        val modelDownload = ModelDownload(
            title = model.title,
            filename = filename,
            id = currentServer,
            url = currentServer,
            thumbnail = Utils.generateThumbnail(model.image, 200),
            directory = Utils.getDownloadPath(context),
        )

        downloadManager.enqueueDownload(modelDownload) { exist ->
            if (exist) {
                onSuccess(modelDownload)
            } else {
                tryDownload(modelDownload, onSuccess, onFailure)
            }
        }
    }

    private fun tryDownload(
        modelDownload: ModelDownload,
        onSuccess: (ModelDownload) -> Unit,
        onFailure: (String) -> Unit
    ) {
        downloadManager.downloadWithErrorHandling(
            modelDownload,
            onSuccess = {
                currentRetries = 0
                onSuccess(it)
            },
            onFailure = { error ->
                incrementRetries()

                if (hasExceededRetries()) {
                    if (shouldTryNextServer()) {
                        moveToNextServer()
                        downloadFile(onSuccess, onFailure)
                    } else {
                        onFailure("All servers are currently unavailable after multiple attempts. Please try again later.")
                    }
                } else {
                    // Retry the same server
                    tryDownload(modelDownload, onSuccess, onFailure)
                }
            }
        )
    }
}

// Extension for DownloadManager with error handling
fun DownloadManager.downloadWithErrorHandling(
    modelDownload: ModelDownload,
    onSuccess: (ModelDownload) -> Unit,
    onFailure: (String) -> Unit
) {
    enqueueDownload(modelDownload) { exist ->
        if (exist) {
            onSuccess(modelDownload)
        } else {
            try {
                // Simulate download logic (replace with actual mechanism)
                onSuccess(modelDownload)
            } catch (e: Exception) {
                onFailure("Download failed: ${e.message}")
            }
        }
    }
}
