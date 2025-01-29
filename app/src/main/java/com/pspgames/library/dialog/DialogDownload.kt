package com.pspgames.library.dialog

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.pspgames.library.R
import com.pspgames.library.activity.ActivityDownload
import com.pspgames.library.ads.AdsUtils
import com.pspgames.library.downloader.DownloadManager
import com.pspgames.library.model.ModelDownload
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.utils.Utils
import com.pspgames.library.utils.isConsole
import com.pspgames.library.utils.logging

class DialogDownload(context: Context, item: ModelLatest.Data) : BaseDialog(context, true) {
    init {
        val downloadManager = DownloadManager(context)
        setCancelable(true)
        setTitle(context.getString(R.string.dialog_download_title))
        setDescrtiption(context.getString(R.string.dialog_download_description))
        setPositiveButton(if (Utils.isMP4(item.image)) "VIDEO" else "IMAGE") {
            AdsUtils.showReward(context as Activity) {
                val filename = Utils.getFileNameFromUrl(item.image)
                val url = Utils.getRealUrl(item.image)
                val modelDownload = ModelDownload(
                    title = item.title,
                    filename = filename,
                    id = url,
                    url = url,
                    thumbnail = Utils.generateThumbnail(item.image, 200),
                    directory = Utils.getDownloadPath(context),
                )
                downloadManager.enqueueDownload(modelDownload) { exist ->
                    if (exist) {
                        dismiss()
                        DialogExist(context, modelDownload).show()
                    } else {
                        dismiss()
                        ActivityDownload.start(context)
                    }
                }
            }
        }
        if(item.isConsole()){
            val items = item.iso.replace("\r", "")
            val isoArray = items.split("\n")
            if(isoArray.isNotEmpty()){
                setNegativeButton("GAME FILE") {
                    dismiss()
                    DialogSelectDownload(context, item, isoArray).show()
                }
            } else {
                Toast.makeText(context, "Server Not Found", Toast.LENGTH_SHORT).show()
            }

        } else {
            val items = item.zipFile1.replace("\r", "")
            val zipArray = items.split("\n")
            if(zipArray.isNotEmpty()){
                setNegativeButton("ZIP") {
                    dismiss()
                    DialogSelectDownload(context, item, zipArray).show()
                }
            } else {
                Toast.makeText(context, "Server Not Found", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun createModel(item: ModelLatest.Data, isZip: Boolean = false): ModelDownload {
        logging(item.isConsole())
        logging(Utils.getFileNameFromUrl(item.image))
        val filename = if (item.isConsole()) {
            Utils.getFileNameFromUrl(item.iso)
        } else {
            if (isZip) Utils.getFileNameFromUrl(item.zipFile1) else Utils.getFileNameFromUrl(item.image)
        }

        val url = if (item.isConsole()) {
            item.iso
        } else {
            if (isZip) item.zipFile1 else Utils.getRealUrl(item.image)
        }
        logging(isZip)
        logging(filename)
        logging(url)
        return ModelDownload(
            title = item.title,
            id = url,
            url = url,
            thumbnail = Utils.generateThumbnail(item.image, 200),
            directory = Utils.getDownloadPath(context),
            filename = filename
        )
    }


}