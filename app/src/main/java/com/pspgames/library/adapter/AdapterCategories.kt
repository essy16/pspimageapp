package com.pspgames.library.adapter

import android.os.Bundle
import android.view.ViewGroup
import coil.load
import com.pspgames.library.activity.ActivityWallpaperList
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.ItemCategoriesBinding
import com.pspgames.library.model.ModelCategories
import com.pspgames.library.utils.UrlUtils
import com.pspgames.library.utils.Utils

class AdapterCategories: BaseRVAdapter<ModelCategories.Data, ItemCategoriesBinding>() {
    override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemCategoriesBinding> {
        return BaseViewHolder(parent.toBinding())
    }

    override fun convert(binding: ItemCategoriesBinding, item: ModelCategories.Data, position: Int) {
        val context = binding.root.context
        binding.categoriesImage.load(UrlUtils.URL_STORAGE + item.image.replace("localhost", "192.168.1.51"))
        binding.categoriesName.text = item.name
        binding.root.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("item", item)
            Utils.startAct(context, ActivityWallpaperList::class.java, bundle)
        }
    }

}