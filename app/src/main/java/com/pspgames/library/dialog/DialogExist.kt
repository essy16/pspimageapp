package com.pspgames.library.dialog

import android.content.Context
import com.pspgames.library.App
import com.pspgames.library.R
import com.pspgames.library.activity.ActivityDownload
import com.pspgames.library.downloader.DownloadManager
import com.pspgames.library.model.ModelDownload

class DialogExist (context: Context, modelDownload: ModelDownload): BaseDialog(context, true) {
    init {
        val downloadManager = DownloadManager(context)
        setCancelable(true)
        setTitle(context.getString(R.string.dialog_exist_title))
        setDescrtiption(context.getString(R.string.dialog_exist_description))
        setNegativeButton(context.getString(R.string.dialog_exist_button1)){
            dismiss()
        }
        setPositiveButton(context.getString(R.string.dialog_exist_button2)){
            downloadManager.replaceDownload(modelDownload){ success ->
                if(success){
                    ActivityDownload.start(context)
                } else {
                    App.toast(context.getString(R.string.download_failed))
                }
                dismiss()
            }
        }
    }
}