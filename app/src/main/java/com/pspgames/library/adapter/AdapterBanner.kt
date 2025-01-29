package com.pspgames.library.adapter

import android.os.Bundle
import android.view.ViewGroup
import coil.load
import com.pspgames.library.Config
import com.pspgames.library.activity.ActivityWallpaperList2
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.ItemBannerBinding
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.utils.Utils

class AdapterBanner(val title: String): BaseRVAdapter<ModelLatest.Data, ItemBannerBinding>() {
    override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemBannerBinding> {
        return BaseViewHolder(parent.toBinding())
    }

    override fun convert(
        binding: ItemBannerBinding,
        item: ModelLatest.Data,
        position: Int
    ) {
        binding.title.text = title
        val thumbnail = when (item.type) {
            "DOUBLE" -> {
                item.imageGif.replace("localhost", "172.30.112.1")
            }
            else -> {
                Utils.generateThumbnail(item.image, 200)
            }
        }
        binding.image.load(thumbnail)
        binding.root.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(ActivityWallpaperList2.DATA, Config.HOME_BANNER.name)
            Utils.startAct(context, ActivityWallpaperList2::class.java, bundle)
        }
    }
}