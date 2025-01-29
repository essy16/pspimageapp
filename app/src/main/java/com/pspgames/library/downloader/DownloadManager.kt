package com.pspgames.library.downloader

import android.content.Context
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.pspgames.library.database.DownloadTable
import com.pspgames.library.enums.DownloadStatus
import com.pspgames.library.interfaces.DownloadListener
import com.pspgames.library.model.ModelDownload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadManager(private val context: Context) {
    private val downloadTable: DownloadTable = DownloadTable.getRepo(context)
    private val workManager: WorkManager = WorkManager.getInstance(context)
    private lateinit var observer: Observer<List<WorkInfo?>?>

    fun setListener(downloadListener: DownloadListener){
        DownloadWorker.setListener(downloadListener)
    }

    fun setObserver(observer: Observer<List<WorkInfo?>?>){
        this.observer = observer
    }

    fun download(){
        getObserver().let {
            DownloadWorker.start(context, it)
        }
    }

    fun cancel(){
        getObserver().let {
            DownloadWorker.cancel(context, it)
        }
    }

    fun onResume(){
        getObserver().let {
            CoroutineScope(Dispatchers.Main).launch {
                DownloadWorker.resume(context, it)
            }        }
    }

    fun onPause(){
        getObserver().let {
            DownloadWorker.pause()
        }
    }

    fun onDestroy(){
        getObserver().let {
            DownloadWorker.removeObserver(context, it)
        }
    }

    private fun getObserver() : Observer<List<WorkInfo?>?>{
        if(::observer.isInitialized){
            return observer
        } else {
            throw Exception("Please call downloadManager.setObserver first")
        }
    }


    fun enqueueDownload(modelDownload: ModelDownload, callback: (Boolean) -> Unit) {
        addDownloadRequestToDatabase(modelDownload, callback)
    }

    fun replaceDownload(modelDownload: ModelDownload, callback: (Boolean) -> Unit) {
        replaceDownloadToDatabase(modelDownload, callback)
    }

    private fun addDownloadRequestToDatabase(modelDownload: ModelDownload, callback: (exist: Boolean) -> Unit) {
        try {
            downloadTable.insert(modelDownload)
            callback.invoke(false)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.invoke(true)
        }
    }

    private fun replaceDownloadToDatabase(modelDownload: ModelDownload, callback: (success: Boolean) -> Unit) {
        try {
            downloadTable.delete(modelDownload.id)
            modelDownload.status = DownloadStatus.ENQUEUE.name
            downloadTable.insert(modelDownload)
            callback.invoke(true)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.invoke(false)
        }
    }
}