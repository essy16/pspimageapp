package com.pspgames.library.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import com.pspgames.library.R
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.ItemConsoleBinding
import com.pspgames.library.dialog.DialogGenre
import com.pspgames.library.model.ModelConsole
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


class AdapterConsole : BaseRVAdapter<ModelConsole, ItemConsoleBinding>() {
    override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemConsoleBinding> {
        return BaseViewHolder(parent.toBinding())
    }

    override fun convert(binding: ItemConsoleBinding, item: ModelConsole, position: Int) {
        binding.name.text = item.name
        Glide.with(binding.image)
            .load(item.image)
            .placeholder(R.drawable.placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    binding.progress.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    binding.progress.visibility = View.GONE
                    return false
                }

            })
            .into(binding.image)
        binding.root.setOnClickListener {
            val hashtagList = item.genre.split(",")
            val hashtag = arrayListOf<String>()
            hashtag.addAll(hashtagList)
            DialogGenre(context, hashtag, item).show()
        }
    }
}