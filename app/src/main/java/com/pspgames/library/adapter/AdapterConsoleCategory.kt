package com.pspgames.library.adapter

import android.view.ViewGroup
import coil.load
import com.pspgames.library.activity.ActivityConsoleList
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.ItemConsoleCategoryBinding
import com.pspgames.library.model.ModelConsoleCategory

class AdapterConsoleCategory : BaseRVAdapter<ModelConsoleCategory, ItemConsoleCategoryBinding>() {
    override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemConsoleCategoryBinding> {
        return BaseViewHolder(parent.toBinding())
    }

    override fun convert(binding: ItemConsoleCategoryBinding, item: ModelConsoleCategory, position: Int) {
        val context = binding.root.context
        binding.image.load(item.image)
        binding.root.setOnClickListener {
            ActivityConsoleList.start(context, item)
        }
    }

}