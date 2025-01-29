package com.pspgames.library.activity

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivityPreviewVideoBinding
import com.yandex.mobile.ads.impl.it

class ActivityPreviewVideo : BaseActivity<ActivityPreviewVideoBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupVideoPlayer()
    }

    private fun setupVideoPlayer(){
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(null)
        binding.videoView.setVideoURI(Uri.parse(intent.getStringExtra("image")!!))
        binding.videoView.requestFocus()
        binding.videoView.setOnPreparedListener {
            it.setVolume(0f, 0f)
            it.isLooping = true
            it.start()
        }
    }
}