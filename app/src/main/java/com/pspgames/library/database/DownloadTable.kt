package com.pspgames.library.database

import android.content.Context
import com.ali77gh.easydata.sqlite.EasyTable
import com.pspgames.library.enums.DownloadStatus
import com.pspgames.library.model.ModelDownload

class DownloadTable (context: Context) : EasyTable<ModelDownload>(context, ModelDownload::class.java, autoSetId = false){
    companion object {
        private var repo: DownloadTable? = null
        fun getRepo(context: Context): DownloadTable {
            if (repo ==null) repo = DownloadTable(context)
            return repo!!
        }
    }



    fun getPendingDownloads() = filter { it.status == DownloadStatus.ENQUEUE.name }

    fun getPendingDownload() = first { it.status == DownloadStatus.ENQUEUE.name }
}