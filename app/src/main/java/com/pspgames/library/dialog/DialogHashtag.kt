package com.pspgames.library.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.beloo.widget.chipslayoutmanager.SpacingItemDecoration
import com.pspgames.library.R
import com.pspgames.library.activity.ActivityConsoleByGenre
import com.pspgames.library.activity.ActivityWallpaperHashtag
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.DialogHashtagBinding
import com.pspgames.library.databinding.ItemHashtagBinding
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.utils.Utils
import com.pspgames.library.utils.isConsole


class DialogHashtag(context: Context, hashtags: ArrayList<String>, modelWallpaper: ModelLatest.Data) :
    Dialog(context, R.style.AlertDialog) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val binding: DialogHashtagBinding = DialogHashtagBinding.inflate(LayoutInflater.from(context))
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
        binding.recyclerView.layoutManager = chipsLayoutManager as RecyclerView.LayoutManager
        binding.recyclerView.addItemDecoration(SpacingItemDecoration(40, 40) as RecyclerView.ItemDecoration)
        binding.recyclerView.adapter = AdapterHashtag(modelWallpaper).apply {
            this.setupData(hashtags)
        }
    }

    inner class AdapterHashtag(private val modelWallpaper: ModelLatest.Data) : BaseRVAdapter<String, ItemHashtagBinding>() {
        override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemHashtagBinding> {
            return BaseViewHolder(parent.toBinding())
        }

        @SuppressLint("SetTextI18n")
        override fun convert(binding: ItemHashtagBinding, item: String, position: Int) {
            binding.hashtag.text = "#" + item.replaceFirstChar {
                it.uppercase()
            }
            binding.root.setOnClickListener {
                dismiss()
                val bundle = Bundle()
                bundle.putString("hashtag", item)
                if (modelWallpaper.isConsole()) {
                    ActivityConsoleByGenre.start(context, item)
                } else {
                    Utils.startAct(context, ActivityWallpaperHashtag::class.java, bundle)
                }
            }
        }

    }
}