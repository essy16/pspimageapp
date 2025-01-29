package com.pspgames.library.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import coil.load
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.pspgames.library.R
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.base.corMain
import com.pspgames.library.databinding.ActivityPreviewBinding
import com.pspgames.library.utils.Utils

class ActivityPreview : BaseActivity<ActivityPreviewBinding>() {
    private lateinit var imagePrimary: String
    private lateinit var imageSecondary: String
    private lateinit var imageType: String
    private val handlerPrimary = Handler(Looper.myLooper()!!)
    private val handlerSecondary = Handler(Looper.myLooper()!!)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageType = intent.getStringExtra("imageType")!!
        imagePrimary = intent.getStringExtra("image")!!
        imageSecondary = intent.getStringExtra("image2")!!
        loadImagePrimary()
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerPrimary.removeCallbacks(runnablePrimary)
        handlerSecondary.removeCallbacks(runnableSecondary)
    }

    private val runnablePrimary = Runnable {
        loadImagePrimary()
    }

    private val runnableSecondary = Runnable {
        loadImageSecondary()
    }

    private fun taskLoadImagePrimary(){
        handlerPrimary.postDelayed(runnablePrimary, 1000)
    }

    private fun taskLoadImageSecondary(){
        handlerSecondary.postDelayed(runnableSecondary, 1000)
    }

    private fun loadImagePrimary(){
        binding.previewImage.load(Utils.getRealUrl(imagePrimary)) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            listener(object : ImageRequest.Listener {
                override fun onStart(request: ImageRequest) {
                    super.onStart(request)
                    binding.progressBar.visibility = View.VISIBLE
                }

                override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                    super.onSuccess(request, result)
                    binding.progressBar.visibility = View.GONE
                    binding.iconGoogle.visibility = View.VISIBLE
                    binding.iconLock.visibility = View.GONE
                    if(imageType == "DOUBLE") {
                        taskLoadImageSecondary()
                    }
                }
            })
        }
    }

    private fun loadImageSecondary(){
        binding.previewImage.load(Utils.getRealUrl(imageSecondary)) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            listener(object : ImageRequest.Listener {
                override fun onStart(request: ImageRequest) {
                    super.onStart(request)
                    binding.progressBar.visibility = View.VISIBLE
                }

                override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                    super.onSuccess(request, result)
                    corMain {
                        binding.progressBar.visibility = View.GONE
                        binding.iconGoogle.visibility = View.GONE
                        binding.iconLock.visibility = View.VISIBLE
                    }
                    taskLoadImagePrimary()
                }
            })
        }

    }
}