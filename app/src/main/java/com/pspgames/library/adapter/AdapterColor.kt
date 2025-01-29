package com.pspgames.library.adapter

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import com.pspgames.library.activity.ActivityByColor
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.ItemColorBinding
import com.pspgames.library.model.ModelColor
import com.pspgames.library.utils.Utils

class AdapterColor: BaseRVAdapter<ModelColor, ItemColorBinding>() {
    override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemColorBinding> {
        return BaseViewHolder(parent.toBinding())
    }

    override fun convert(binding: ItemColorBinding, item: ModelColor, position: Int) {
        binding.color.text = item.name
        var color = item.color
        color = color.replace("#", "")
        color = "#90$color"
        binding.backround.setCardBackgroundColor(Color.parseColor(color))
        binding.root.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("item", item)
            Utils.startAct(context, ActivityByColor::class.java, bundle)
        }
    }
}