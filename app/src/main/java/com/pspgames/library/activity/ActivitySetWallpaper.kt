package com.pspgames.library.activity

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import com.pspgames.library.App
import com.pspgames.library.Config
import com.pspgames.library.R
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivitySetWallpaperBinding
import com.pspgames.library.dialog.DialogSetWallpaper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

@Suppress("DEPRECATION")
class ActivitySetWallpaper :  BaseActivity<ActivitySetWallpaperBinding>() {
    private var width: Int = 0
    private var height: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loading.show()
        setupImage()
        setupButton()
        binding.toolbar.title = "Set Wallpaper"
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupImage(){
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        height = metrics.heightPixels
        width = metrics.widthPixels
        val image = intent.getStringExtra("image")!!
        Glide.with(this)
            .asBitmap()
            .load(getImageUrl(image))
            .placeholder(R.drawable.placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Bitmap>{
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    runOnUiThread {
                        loading.dismiss()
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    runOnUiThread {
                        loading.dismiss()
                        binding.CropImageView.setImageBitmap(resource)
                        binding.CropImageView.setAspectRatio(width, height)
                    }

                    return false
                }

            }).submit()
    }
    private fun getImageUrl(string: String) = if(string.startsWith("http")){
        string
    } else {
        Config.SERVER_URL + "/" + string
    }

    private fun setupButton(){
        binding.buttonRotateLeft.setOnClickListener {
            binding.CropImageView.rotateImage(-90)
        }

        binding.buttonRotateRight.setOnClickListener {
            binding.CropImageView.rotateImage(90)
        }

        binding.buttonAspectRatio.setOnClickListener {
            binding.CropImageView.resetCropRect()
            if (binding.CropImageView.isFixAspectRatio) {
                binding.CropImageView.clearAspectRatio()
                App.toast(getString(R.string.aspect_ratio_unlocked))
            } else {
                binding.CropImageView.setAspectRatio(width, height)
                App.toast(getString(R.string.aspect_ratio_lock))
            }
        }

        binding.buttonFlipHorizontal.setOnClickListener {
            binding.CropImageView.flipImageHorizontally()
        }

        binding.buttonFlipVertical.setOnClickListener {
            binding.CropImageView.flipImageVertically()
        }

        binding.buttonDone.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.CropImageView.getCroppedImage()?.let { it1 ->
                    DialogSetWallpaper(this, it1){
                        finish()
                    }.show()
                }
            } else {
                setWallpaper()
            }
        }
    }

    private fun setWallpaper(){
        try {
            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            wallpaperManager.setBitmap(binding.CropImageView.croppedImage)
            App.toast(getString(R.string.wallpaper_set_success))
            finish()
        } catch (e: Exception) {
            App.toast(getString(R.string.wallpaper_set_failed))
        }
    }

}