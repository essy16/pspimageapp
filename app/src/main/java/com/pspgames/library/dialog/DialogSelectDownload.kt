package com.pspgames.library.dialog

import ServerSelectionManager
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
): BaseRVAdapter<String, DialogSelectButtonBinding>() {

    override fun viewHolder(parent: ViewGroup): BaseViewHolder<DialogSelectButtonBinding> {
        return BaseViewHolder(parent.toBinding())
    }

    @SuppressLint("SetTextI18n")
    override fun convert(binding: DialogSelectButtonBinding, item: String, position: Int) {
        binding.button.text = "SERVER #${position + 1}"
        binding.button.setOnClickListener {
            AdsUtils.showReward(context as Activity) {
                // Always start with Server 1 (first in the list)
                val serverSelectionManager = ServerSelectionManager(
                    context,
                    model,
                    listOf(data[0]) + data.filterIndexed { index, _ -> index != 0 }
                )

                serverSelectionManager.downloadFile(
                    onSuccess = { modelDownload ->
                        callback.invoke()
                        ActivityDownload.start(context)
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}