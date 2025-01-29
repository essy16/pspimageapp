package com.pspgames.library.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import coil.load
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.pspgames.library.App
import com.pspgames.library.R
import com.pspgames.library.activity.ActivityDetail
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.ItemWallpaperBinding
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.utils.Utils
import com.pspgames.library.utils.isConsole


class AdapterLatest: BaseRVAdapter<ModelLatest.Data, ItemWallpaperBinding>() {
    override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemWallpaperBinding> {
        return BaseViewHolder(parent.toBinding())
    }

    @SuppressLint("CheckResult")
    override fun convert(binding: ItemWallpaperBinding, item: ModelLatest.Data, position: Int) {
        val context = binding.root.context
        val thumbnail = when (item.type) {
            "DOUBLE" -> {
                item.imageGif
            }
            else -> {
                Utils.generateThumbnail(item.image, 200)
            }
        }
        binding.title.text = item.title
        binding.wallpaperImage.load(thumbnail) {
            crossfade(false)
            placeholder(R.drawable.placeholder)
            listener(
                onError = { _: ImageRequest, result: ErrorResult ->
                    App.log(thumbnail)
                    result.throwable.message?.let { App.log(it) }
                    binding.progressBar.visibility = View.GONE
                },
                onSuccess = { _: ImageRequest, _: SuccessResult ->
                    binding.progressBar.visibility = View.GONE
                }
            )
        }

        binding.wallpaperView.text = item.view.toString()
        binding.wallpaperPremium.visibility = if(item.premium == 0) View.GONE else View.VISIBLE
        binding.cardType.visibility = if(item.type == "IMAGE") View.GONE else View.VISIBLE
        when (item.type) {
            "VIDEO" -> {
                binding.wallpaperType.text = context.getString(R.string.type_video)
            }
            "GIF" -> {
                binding.wallpaperType.text = context.getString(R.string.type_gif)
            }
            else -> {
                binding.wallpaperType.text = context.getString(R.string.type_double)
            }
        }
        if(item.isConsole()){
            binding.cardType.visibility = View.GONE
        }
        binding.root.setOnClickListener {
            data[position].view += 1
            notifyItemChanged(position)
            val trimList =  ArrayList<ModelLatest.Data>(data.subList(position, data.size))
            if(item.premium == 0){
                val bundle = Bundle()
                bundle.putParcelableArrayList("item", trimList)
                bundle.putInt("position", position)
                Utils.startAct(context, ActivityDetail::class.java, bundle)
            } else {
                val bundle = Bundle()
                bundle.putParcelableArrayList("item", trimList)
                bundle.putInt("position", position)
                Utils.startAct(context, ActivityDetail::class.java, bundle)
            }

        }
    }

}