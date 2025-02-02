package com.pspgames.library.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.pspgames.library.R
import com.pspgames.library.activity.ActivityDownload
import com.pspgames.library.ads.AdsUtils
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.DialogSelectButtonBinding
import com.pspgames.library.databinding.DialogSelectDownloadBinding
import com.pspgames.library.downloader.DownloadManager
import com.pspgames.library.model.ModelDownload
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.utils.Utils
import com.pspgames.library.utils.logging
import java.util.LinkedList
import java.util.Queue


class DialogSelectDownload(context: Context, model: ModelLatest.Data, isoArray: List<String>) : Dialog(context, R.style
    .AlertDialog) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val binding = DialogSelectDownloadBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = AdapterSelectDownload(model){
            dismiss()
        }.apply {
            setupData(ArrayList(isoArray))
        }
    }
}

class AdapterSelectDownload(
    private val model: ModelLatest.Data,
    private val callback: () -> Unit
) : BaseRVAdapter<String, DialogSelectButtonBinding>() {

    private val serverQueue: Queue<String> = LinkedList()

    init {
        setupServerQueue()
    }

    override fun viewHolder(parent: ViewGroup): BaseViewHolder<DialogSelectButtonBinding> {
        return BaseViewHolder(parent.toBinding())
    }

    @SuppressLint("SetTextI18n")
    override fun convert(binding: DialogSelectButtonBinding, item: String, position: Int) {
        binding.button.text = "SERVER #${position + 1}"
        binding.button.setOnClickListener {
            attemptDownload()
        }
    }

    private fun setupServerQueue() {
        serverQueue.clear()
        serverQueue.addAll(data)  // Initialize the queue with available servers
    }

    private fun attemptDownload() {
        if (serverQueue.isEmpty()) {
            Toast.makeText(context, "All servers failed. Please try again later.", Toast.LENGTH_SHORT).show()
            callback.invoke()
            return
        }

        val server = serverQueue.poll() // Get the next server
        val filename = if (Utils.isIso(server)) "${model.title}.iso" else "${model.title}.zip"

        val modelDownload = ModelDownload(
            title = model.title,
            filename = filename,
            id = server,
            url = server,
            thumbnail = Utils.generateThumbnail(model.image, 200),
            directory = Utils.getDownloadPath(context),
        )

        logging("Attempting download from: $server")

        AdsUtils.showReward(context as Activity) {
            DownloadManager(context).enqueueDownload(modelDownload) { exist ->
                if (exist) {
                    callback.invoke()
                    DialogExist(context, modelDownload).show()
                } else {
                    callback.invoke()
                    ActivityDownload.start(context)
                }
            }
        }
    }
}
