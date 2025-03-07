package com.pspgames.library.adapter

import DialogDownload
import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pspgames.library.App
import com.pspgames.library.R
import com.pspgames.library.activity.ActivityPreview
import com.pspgames.library.activity.ActivityPreviewVideo
import com.pspgames.library.activity.ActivitySetWallpaper
import com.pspgames.library.ads.AdmobUtils
import com.pspgames.library.ads.AdsUtils
import com.pspgames.library.base.BaseMultipleAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.ItemDetailBinding
import com.pspgames.library.databinding.ItemNativeDetailBinding
import com.pspgames.library.dialog.DialogHashtag
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.network.MainViewModel
import com.pspgames.library.services.VideoWallpaperService
import com.pspgames.library.utils.Converter
import com.pspgames.library.utils.Utils
import com.pspgames.library.utils.Utils.downloadToInternal
import com.pspgames.library.utils.Utils.downloadVideoForSet
import com.pspgames.library.utils.Utils.downloadVideoToCache
import com.pspgames.library.utils.Utils.isImage
import com.pspgames.library.utils.Utils.isMP4
import com.pspgames.library.utils.Utils.setWallpaperAsGif
import com.pspgames.library.utils.WallpaperSetUtils
import com.pspgames.library.utils.isConsole
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.io.File

class AdapterDetail(val viewModel: MainViewModel): BaseMultipleAdapter<ModelLatest.Data>() {
    companion object {
        const val VIEW_ITEM = 0
        const val VIEW_NATIVE = 1
    }
    override fun viewHolder(parent: ViewGroup): ArrayList<RecyclerView.ViewHolder> {
        return arrayListOf(MainViewHolder(parent.toBinding()), NativeViewHolder(parent.toBinding()))
    }

    override fun convert(
        viewHolder: RecyclerView.ViewHolder,
        item: ModelLatest.Data,
        position: Int,
        viewType: Int
    ) {
        if(item.type == "native"){
            val holder = viewHolder as NativeViewHolder
            val binding = holder.binding
            binding.nativeAdsTemplate.setNativeAd(AdmobUtils.getNative())
        } else {
            val holder = viewHolder as MainViewHolder
            val binding = holder.binding
            val context = binding.root.context
            binding.wallpaperPremium.visibility = if(item.premium == 0) View.GONE else View.VISIBLE
            binding.buttonPlay.visibility = if(item.type == "VIDEO") View.VISIBLE else View.GONE
            setIsFavourite(position, binding)
            setIsCollection(position, binding)
            val thumbnail = when (item.type) {
                "DOUBLE" -> {
                    item.imageGif.replace("localhost", "172.30.112.1")
                }
                else -> {
                    Utils.generateThumbnail(item.image, 200)
                }
            }
            Glide.with(binding.detailImage)
                .load(thumbnail)
                .placeholder(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        binding.buttonDownload.visibility = View.VISIBLE
                        binding.buttonHashtag.visibility = View.VISIBLE
                        binding.layoutPreview.visibility = View.VISIBLE
                        binding.buttonApply.setOnClickListener {
                            if(item.type == "DOUBLE"){
                                if(item.premium == 0){
                                    WallpaperSetUtils.setHomeScreen(context, item.image){
                                        WallpaperSetUtils.setLockScreen(context, item.image2){
                                            AdsUtils.showInterstitial(context as Activity){
                                                App.toast(context.getString(R.string.wallpaper_set_success))
                                            }
                                        }
                                    }
                                } else {
                                    AdsUtils.showReward(context as Activity) {
                                        WallpaperSetUtils.setHomeScreen(context, item.image){
                                            WallpaperSetUtils.setLockScreen(context, item.image2){
                                                App.toast(context.getString(R.string.wallpaper_set_success))
                                            }
                                        }
                                    }
                                }
                            } else {
                                if(Utils.isGif(item.image)){
                                    if(item.premium == 0){
                                        setWallpaperAsGif(item.image, context)
                                    } else {
                                        AdsUtils.showReward(context as Activity) {
                                            setWallpaperAsGif(item.image, context)
                                        }
                                    }
                                } else if(Utils.isMP4(item.image)){
                                    if(item.premium == 0){
                                        downloadVideoForSet(item.image, context, binding) {
                                            VideoWallpaperService.start(context)
                                        }
                                    } else {
                                        AdsUtils.showReward(context as Activity) {
                                            downloadVideoForSet(item.image, context, binding) {
                                                VideoWallpaperService.start(context)
                                            }
                                        }
                                    }
                                } else {
                                    if(item.premium == 0){
                                        val bundle = Bundle()
                                        bundle.putString("image", item.image)
                                        Utils.startAct(context, ActivitySetWallpaper::class.java, bundle)
                                    } else {
                                        AdsUtils.showReward(context as Activity) {
                                            val bundle = Bundle()
                                            bundle.putString("image", item.image)
                                            Utils.startAct(context, ActivitySetWallpaper::class.java, bundle)
                                        }
                                    }
                                }
                            }
                        }

                        binding.buttonHashtag.setOnClickListener {
                            if(item.isConsole()) {
                                val hashtagList = item.genre.split(",")
                                val hashtag = arrayListOf<String>()
                                hashtag.addAll(hashtagList)
                                DialogHashtag(context, hashtag, item).show()
                            } else {
                                val hashtagList = item.tags.split(",")
                                val hashtag = arrayListOf<String>()
                                hashtag.addAll(hashtagList)
                                DialogHashtag(context, hashtag, item).show()
                            }
                        }

                        binding.buttonColor.setOnClickListener {
                            val hashtagList = item.color.split(",")
                            val hashtag = arrayListOf<String>()
                            hashtag.addAll(hashtagList)
                            DialogHashtag(context, hashtag, item).show()
                        }

                        binding.buttonDownload.setOnClickListener {
                            DialogDownload(context, item).show()
                        }

                        binding.buttonFavourite.setOnClickListener {
                            setFavourite(position, binding)
                        }

                        binding.buttonCollection.setOnClickListener {
                            if(!item.isConsole()){
                                if(!isImage(item.image)){
                                    App.toast(binding.root.context.getString(R.string.collection_error))
                                    return@setOnClickListener
                                }
                                if(item.premium == 0){
                                    setCollection(position, binding)
                                } else {
                                    AdsUtils.showReward(context as Activity) {
                                        setCollection(position, binding)
                                    }
                                }
                            }
                        }
                        if(item.isConsole()){
                            binding.buttonColor.visibility = View.GONE
                            binding.buttonCollectionRoot.visibility = View.INVISIBLE
                            item.type = "IMAGE"
                        }
                        binding.root.setOnClickListener {
                            if(isMP4(item.image)){
                                downloadVideoToCache(item.image, context, binding){
                                    val bundle = Bundle()
                                    bundle.putString("image", it)
                                    Utils.startAct(context, ActivityPreviewVideo::class.java, bundle)
                                }
                            } else {
                                val bundle = Bundle()
                                bundle.putString("image", item.image)
                                bundle.putString("image2", item.image2)
                                bundle.putString("imageType", item.type)
                                Utils.startAct(context, ActivityPreview::class.java, bundle)
                            }
                        }
                        return false
                    }

                })
                .into(binding.detailImage)
        }
    }

    override fun setViewType(position: Int): Int {
        if(data[position].type == "native"){
            return VIEW_NATIVE
        }
        return VIEW_ITEM
    }

    inner class MainViewHolder(binding: ItemDetailBinding): BaseViewHolder<ItemDetailBinding>(binding)
    inner class NativeViewHolder(binding: ItemNativeDetailBinding): BaseViewHolder<ItemNativeDetailBinding>(binding)


    private fun setIsFavourite(position: Int, binding: ItemDetailBinding){
        val id = data[position].id
        try {
            App.favouriteDatabase.getById(id).image
            binding.buttonFavourite.setImageResource(R.drawable.ic_detail_love_fill)
        } catch (e: Exception) {
            binding.buttonFavourite.setImageResource(R.drawable.ic_detail_love)
        }
    }

    private fun setFavourite(position: Int, binding: ItemDetailBinding){
        try {
            App.favouriteDatabase.insert(data[position])
            binding.buttonFavourite.setImageResource(R.drawable.ic_detail_love_fill)
            App.toast(binding.root.context.getString(R.string.favourite_added))
        } catch (e: Exception) {
            App.favouriteDatabase.delete(data[position].id)
            binding.buttonFavourite.setImageResource(R.drawable.ic_detail_love)
            App.toast(binding.root.context.getString(R.string.favourite_remove))
        }
    }

    private fun setIsCollection(position: Int, binding: ItemDetailBinding){
        val id = data[position].id
        try {
            App.collectionTable.getById(id).image
            binding.buttonCollection.setImageResource(R.drawable.ic_detail_start_fill)
        } catch (e: Exception) {
            binding.buttonCollection.setImageResource(R.drawable.ic_detail_start)
        }
    }

    private fun setCollection(position: Int, binding: ItemDetailBinding){
        val item = data[position]
        try {
            val localImage = App.collectionTable.getById(item.id).localImage
            val file = File(localImage)
            if(file.exists()) file.delete()
            App.collectionTable.delete(item.id)
            binding.buttonCollection.setImageResource(R.drawable.ic_detail_start)
            App.toast(binding.root.context.getString(R.string.collection_remove))
        } catch (e: Exception) {
            downloadToInternal(item.image, binding.root.context, binding, File(binding.root.context.filesDir, "auto_wallpaper").absolutePath,
                scanning = false,
                toast = false, onSuccess = {
                    App.collectionTable.insert(Converter.favouriteToCollection(data[position], it))
                    binding.buttonCollection.setImageResource(R.drawable.ic_detail_start_fill)
                    App.toast(binding.root.context.getString(R.string.collection_added))
                }
            )
        }
    }

}