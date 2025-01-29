package com.pspgames.library.activity

import android.content.Context
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import coil.load
import coil.request.videoFrameMillis
import com.pspgames.library.App
import com.pspgames.library.Config
import com.pspgames.library.R
import com.pspgames.library.ads.AdmobUtils
import com.pspgames.library.ads.AdsUtils
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.base.handler
import com.pspgames.library.custom.BlurTransformation
import com.pspgames.library.databinding.ActivitySingleWallpaperBinding
import com.pspgames.library.dialog.DialogHashtag
import com.pspgames.library.dialog.DialogProgress
import com.pspgames.library.enums.AdsProvider
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.network.Status
import com.pspgames.library.services.GifWallpaperService
import com.pspgames.library.services.VideoWallpaperService
import com.pspgames.library.utils.PrefsUtils
import com.pspgames.library.utils.Utils
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.pixplicity.easyprefs.library.Prefs
import java.io.File
import java.util.*

@Suppress("DEPRECATION")
class ActivitySingleWallpaper : BaseActivity<ActivitySingleWallpaperBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }
        if(PrefsUtils.getAdmob().provider == AdsProvider.ADMOB.name){
            if(PrefsUtils.getAdmob().nativeEnable == 1){
                AdsUtils.showBanner(this, binding.adsView)
            } else {
                if(!AdmobUtils.nativeIsLoaded()){
                    AdsUtils.showBanner(this, binding.adsView)
                }
            }
        } else {
            AdsUtils.showBanner(this, binding.adsView)
        }
        val id = intent.getStringExtra("id").toString()
        getData(id){
            initView(it)
        }
        binding.buttonBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initView(item: ModelLatest.Data){
        binding.wallpaperPremium.visibility = if(item.premium == 0) View.GONE else View.VISIBLE
        binding.buttonPlay.visibility = if(item.type == "VIDEO") View.VISIBLE else View.GONE
        loadBackground(item.image)
        val thumbnail = when (item.type) {
            "DOUBLE" -> {
                item.imageGif.replace("localhost", "172.30.112.1")
            }
            else -> {
                Utils.generateThumbnail(item.image, 200)
            }
        }
        binding.detailImage.load(thumbnail) {
            placeholder(R.drawable.placeholder)
            videoFrameMillis(1)
            listener(
                onSuccess = { _, _ ->
                    binding.buttonDownload.visibility = View.VISIBLE
                    binding.buttonHashtag.visibility = View.VISIBLE
                    binding.layoutPreview.visibility = View.VISIBLE
                    binding.buttonApply.setOnClickListener {
                        if(item.premium == 0){
                            when (item.type) {
                                "GIF" -> {
                                    setWallpaperAsGif(item.image, this@ActivitySingleWallpaper)
                                }
                                "VIDEO" -> {
                                    downloadVideoToCache(item.image, this@ActivitySingleWallpaper) {
                                        Prefs.putString("video_file", it)
                                        VideoWallpaperService.start(this@ActivitySingleWallpaper)
                                    }
                                }
                                else -> {
                                    val bundle = Bundle()
                                    bundle.putString("image", item.image)
                                    Utils.startAct(this@ActivitySingleWallpaper, ActivitySetWallpaper::class.java, bundle)
                                }
                            }
                        } else {
                            AdsUtils.showReward(this@ActivitySingleWallpaper) {
                                when (item.type) {
                                    "GIF" -> {
                                        setWallpaperAsGif(item.image, this@ActivitySingleWallpaper)
                                    }
                                    "VIDEO" -> {
                                        downloadVideoToCache(item.image, this@ActivitySingleWallpaper) {
                                            Prefs.putString("video_file", it)
                                            VideoWallpaperService.start(this@ActivitySingleWallpaper)
                                        }
                                    }
                                    else -> {
                                        val bundle = Bundle()
                                        bundle.putString("image", item.image)
                                        Utils.startAct(this@ActivitySingleWallpaper, ActivitySetWallpaper::class.java, bundle)
                                    }
                                }
                            }
                        }
                    }

                    binding.buttonHashtag.setOnClickListener {
                        val hashtagList = item.tags.split(",")
                        val hashtag = arrayListOf<String>()
                        hashtag.addAll(hashtagList)
                        DialogHashtag(this@ActivitySingleWallpaper, hashtag, item).show()
                    }

                    binding.buttonColor.setOnClickListener {
                        val hashtagList = item.color.split(",")
                        val hashtag = arrayListOf<String>()
                        hashtag.addAll(hashtagList)
                        DialogHashtag(this@ActivitySingleWallpaper, hashtag, item).show()
                    }

                    binding.buttonDownload.setOnClickListener {
                        if(item.premium == 0){
                            AdsUtils.showInterstitial(this@ActivitySingleWallpaper){
                                addDownload(item.id)
                                downloadWallpaper(item.image, this@ActivitySingleWallpaper)
                            }
                        } else {
                            AdsUtils.showReward(this@ActivitySingleWallpaper){
                                addDownload(item.id)
                                downloadWallpaper(item.image, this@ActivitySingleWallpaper)
                            }
                        }
                    }

                    binding.buttonFavourite.setOnClickListener {
                        setFavourite(item)
                    }

                    binding.root.setOnClickListener {
                        when (item.type) {
                            "VIDEO" -> {
                                downloadVideoToCache(item.image, this@ActivitySingleWallpaper){
                                    val bundle = Bundle()
                                    bundle.putString("image", it)
                                    Utils.startAct(this@ActivitySingleWallpaper, ActivityPreviewVideo::class.java, bundle)
                                }
                            }
                            else -> {
                                val bundle = Bundle()
                                bundle.putString("image", item.image)
                                Utils.startAct(this@ActivitySingleWallpaper, ActivityPreview::class.java, bundle)
                            }
                        }
                    }
                }
            )
        }

    }

    private fun getData(id: String, callback: (ModelLatest.Data) -> Unit){
        val params: MutableMap<String, String> = HashMap()
        params["id"] = id
        viewModel.getSingle(params).observe(this) { status ->
            when (status.status) {
                Status.SUCCESS -> {
                    callback.invoke(status.data!!)
                    loading.dismiss()
                }
                Status.LOADING -> {
                    loading.show()
                }
                Status.ERROR -> {
                    loading.dismiss()
                }
            }
        }
    }

    override fun onBackPressed() {
        startUi()
    }

    private fun startUi(){
        if(Config.UI_VERSION == 1){
            Utils.startAct(this@ActivitySingleWallpaper, MainActivity::class.java)
            finish()
        } else {
            Utils.startAct(this@ActivitySingleWallpaper, MainActivity2::class.java)
            finish()
        }
    }

    private fun addDownload(id: String){
        val params: MutableMap<String, String> = java.util.HashMap()
        params["id"] = id
        viewModel.addView(params)
    }

    private fun setIsFavourite(item: ModelLatest.Data){
        val id = item.id
        try {
            App.favouriteDatabase.getById(id).image
            binding.buttonFavourite.setImageResource(R.drawable.ic_detail_love_fill)
        } catch (e: Exception) {
            binding.buttonFavourite.setImageResource(R.drawable.ic_detail_love)
        }
    }

    private fun setFavourite(item: ModelLatest.Data){
        try {
            App.favouriteDatabase.insert(item)
            binding.buttonFavourite.setImageResource(R.drawable.ic_detail_love_fill)
        } catch (e: Exception) {
            App.favouriteDatabase.delete(item.id)
            binding.buttonFavourite.setImageResource(R.drawable.ic_detail_love)
        }
    }

    private fun downloadVideoToCache(url: String, context: Context, callback: (String) -> Unit){
        val dirPath = context.getExternalFilesDir("VIDEO")!!.absolutePath
        val fileName = "video_wallpaper.mp4"
        binding.progressBar.max = 100f
        binding.buttonPlay.visibility = View.GONE
        binding.layoutProgress.visibility = View.VISIBLE
        PRDownloader.download(url, dirPath,  fileName)
            .build()
            .setOnProgressListener {
                handler {
                    val progress = (it.currentBytes * 100) / it.totalBytes
                    binding.progressBar.progress = progress.toFloat()
                }
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    binding.buttonPlay.visibility = View.VISIBLE
                    binding.layoutProgress.visibility = View.GONE
                    callback.invoke(File(dirPath, fileName).absolutePath)
                }
                override fun onError(error: com.downloader.Error?) {
                    App.toast(context.getString(R.string.download_failed))
                    binding.buttonPlay.visibility = View.VISIBLE
                    binding.layoutProgress.visibility = View.GONE
                }
            })
    }

    private fun setWallpaperAsGif(url: String, context: Context){
        val dialogProgress = DialogProgress(context)
        dialogProgress.show()
        PRDownloader.download(url, context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath, "image.gif")
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    try {
                        GifWallpaperService.start(context)
                        dialogProgress.dismiss()
                    } catch (e: Exception) {
                        App.toast(context.getString(R.string.something_error))
                        dialogProgress.dismiss()
                    }
                }
                override fun onError(error: com.downloader.Error?) {
                    App.toast(context.getString(R.string.something_error))
                }
            })
    }

    private fun downloadWallpaper(url: String, context: Context) {
        downloadToInternal(url, context)
    }

    private fun downloadToInternal(url: String, context: Context){
        val extension = url.substring(url.lastIndexOf("."))
        val dirPath = "/storage/emulated/0/download/" + context.getString(R.string.app_name)
        val fileName = "${UUID.randomUUID()}$extension"
        PRDownloader.download(url, dirPath,  fileName)
            .build()
            .setOnProgressListener {
                binding.progressBar.max = 100f
                binding.layoutProgress.visibility = View.VISIBLE
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    binding.layoutProgress.visibility = View.GONE
                    App.toast(context.getString(R.string.download_success))
                    MediaScannerConnection.scanFile(
                        context, arrayOf(File(dirPath, fileName).absolutePath),
                        null
                    ) { _, _ -> }
                }
                override fun onError(error: com.downloader.Error?) {
                    binding.layoutProgress.visibility = View.GONE
                    App.toast(context.getString(R.string.download_failed))
                }
            })
    }

    private fun loadBackground(url: String){
        binding.backgroundImage.load(Utils.generateThumbnail(url, 200)) {
            transformations(BlurTransformation(this@ActivitySingleWallpaper))
        }
    }
}