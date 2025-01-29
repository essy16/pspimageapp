package com.pspgames.library.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.beloo.widget.chipslayoutmanager.SpacingItemDecoration
import com.pspgames.library.R
import com.pspgames.library.activity.ActivityConsoleByGenre
import com.pspgames.library.activity.ActivityDownload
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.DialogGenreBinding
import com.pspgames.library.databinding.ItemGenreBinding
import com.pspgames.library.downloader.DownloadManager
import com.pspgames.library.model.ModelConsole
import com.pspgames.library.model.ModelDownload
import com.pspgames.library.utils.Utils

class DialogGenre(context: Context, hashtags: ArrayList<String>, item: ModelConsole) : Dialog(context, R.style.AlertDialog) {
    init {
        val downloadManager = DownloadManager(context)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val binding: DialogGenreBinding = DialogGenreBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        val chipsLayoutManager = ChipsLayoutManager.newBuilder(context)
            .setChildGravity(Gravity.TOP)
            .setScrollingEnabled(true)
            .setGravityResolver { Gravity.CENTER }
            .setOrientation(ChipsLayoutManager.HORIZONTAL)
            .setRowStrategy(ChipsLayoutManager.STRATEGY_CENTER_DENSE)
            .withLastRow(true)
            .build()
        binding.name.text = item.name
        binding.recyclerView.layoutManager = chipsLayoutManager as RecyclerView.LayoutManager
        binding.recyclerView.addItemDecoration(SpacingItemDecoration(40, 40) as RecyclerView.ItemDecoration)
        binding.recyclerView.adapter = AdapterHashtag().apply {
            this.setupData(hashtags)
        }
        binding.download.setOnClickListener {
            val modelDownload = createModel(item)
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

    private fun createModel(item: ModelConsole): ModelDownload {
        val extension = item.iso.substring(item.iso.lastIndexOf("."))
        return ModelDownload(
            title = item.name,
            id = item.iso,
            url = item.iso,
            thumbnail = item.image,
            directory = Utils.getDownloadPath(context),
            filename = item.name + extension
        )
    }

    inner class AdapterHashtag : BaseRVAdapter<String, ItemGenreBinding>() {
        override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemGenreBinding> {
            return BaseViewHolder(parent.toBinding())
        }

        @SuppressLint("SetTextI18n")
        override fun convert(binding: ItemGenreBinding, item: String, position: Int) {
            binding.hashtag.text = item
            binding.root.setOnClickListener {
                dismiss()
                ActivityConsoleByGenre.start(context, item)
            }
        }

    }
}