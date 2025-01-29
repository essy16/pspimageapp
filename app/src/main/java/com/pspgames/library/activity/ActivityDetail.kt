package com.pspgames.library.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.pspgames.library.Config
import com.pspgames.library.adapter.AdapterDetail
import com.pspgames.library.ads.AdmobUtils
import com.pspgames.library.ads.AdsUtils
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivityDetailBinding
import com.pspgames.library.enums.AdsProvider
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.utils.PrefsUtils
import com.pspgames.library.utils.Utils
import com.pspgames.library.widget.SliderTransformer
import com.pixplicity.easyprefs.library.Prefs


@Suppress("DEPRECATION")
class ActivityDetail : BaseActivity<ActivityDetailBinding>() {
    private lateinit var adapterDetail: AdapterDetail
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }
        adapterDetail = AdapterDetail(viewModel)
        setupViewPager()
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
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun setupViewPager(){
        val itemList: ArrayList<ModelLatest.Data> = intent.getParcelableArrayListExtra("item")!!
        if(PrefsUtils.getAdmob().nativeEnable == 1 && PrefsUtils.getAdmob().provider == AdsProvider.ADMOB.name && AdmobUtils.nativeIsLoaded()){
            for (i in 0 until  itemList.size){
                if(i % Config.NATIVE_DETAIL_SHOW_AFTER == 0 && i != 0){
                    itemList.add(i , ModelLatest.Data(
                        cid = "",
                        color = "",
                        download = "",
                        form = "",
                        genre = "",
                        id = "",
                        image = "",
                        image2 = "",
                        imageGif = "",
                        iso = "",
                        premium = 0,
                        tags = "",
                        title = "native",
                        type = "",
                        view = 0,
                        zipFile1 = "",
                        zipFile2 = "",
                        zipType = ""
                    ))
                }
            }
        }
        binding.viewPager.adapter = adapterDetail
        binding.viewPager.clipToPadding = false
        binding.viewPager.clipChildren = false
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.setPageTransformer(SliderTransformer(3))
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(adapterDetail.getItemViewType(position) != AdapterDetail.VIEW_NATIVE){
                    try {
                        addView(itemList[position])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if(!Prefs.getBoolean("blurred_background", false)){
                        loadBackground(itemList[position].image)
                    } else {
                        binding.overlay.visibility = View.GONE
                    }
                }
            }
        })

        adapterDetail.setupData(itemList)
    }

    private fun addView(data: ModelLatest.Data){
        try {
            val params: MutableMap<String, String> = HashMap()
            params["id"] = data.id
            params["form"] = data.form
            viewModel.addView(params)
        } catch (e: Exception) {
        }
    }

    private fun loadBackground(url: String){
        binding.backgroundImage.load(Utils.generateThumbnail(url, 200)) {
            transformations(com.pspgames.library.custom.BlurTransformation(this@ActivityDetail))
        }
    }
}