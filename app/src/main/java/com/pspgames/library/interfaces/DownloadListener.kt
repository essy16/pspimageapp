package com.pspgames.library.interfaces

import com.pspgames.library.model.ModelDownload

interface DownloadListener {
    fun onCancel(modelDownload: ModelDownload)
    fun onPause(modelDownload: ModelDownload)
    fun onResume(modelDownload: ModelDownload)
    fun onComplete(modelDownload: ModelDownload)
//    fun onProgress(modelDownload: ModelDownload)
}