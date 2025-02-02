package com.pspgames.library.model

import com.ali77gh.easydata.sqlite.Model
import com.pspgames.library.enums.DownloadStatus
import java.io.File
import java.io.Serializable

data class ModelDownload(
    override var id: String = "",
    val title: String = "",
    val url: String = "",
    var thumbnail: String = "",
    var progress: Int = -1,
    var directory: String = "",
    var filename: String = "",
    var downloaded: Long = -1,
    var paused : Long = -1,
    var total: Long = -1,
    var resumed:Long = -1,
    var status: String = DownloadStatus.ENQUEUE.name,
): Model, Serializable {
    @Suppress("unused")
    fun getStatus() : DownloadStatus{
        return when(status){
            DownloadStatus.COMPLETED.name -> DownloadStatus.COMPLETED
            DownloadStatus.RESUMED.name -> DownloadStatus.RESUMED
            DownloadStatus.FAILED.name -> DownloadStatus.FAILED
            DownloadStatus.CANCELED.name -> DownloadStatus.CANCELED
            DownloadStatus.PAUSED.name -> DownloadStatus.PAUSED
            DownloadStatus.ADCOMPLETED.name -> DownloadStatus.ADCOMPLETED
            else -> DownloadStatus.ENQUEUE
        }
    }

    fun createFile(): File {
        return File(directory, filename)
    }
}
