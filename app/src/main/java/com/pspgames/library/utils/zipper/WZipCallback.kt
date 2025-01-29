package com.pspgames.library.utils.zipper

import androidx.documentfile.provider.DocumentFile
import java.lang.Exception

interface WZipCallback {

    enum class Mode {
        ZIP,
        UNZIP
    }
    fun onStart(worker: String, mode: Mode)
    fun onZipComplete(worker: String, zipFile: DocumentFile)

    fun onUnzipComplete(worker: String, extractedFolder: DocumentFile)
    fun onError(worker: String, e: Exception, mode: Mode)
}