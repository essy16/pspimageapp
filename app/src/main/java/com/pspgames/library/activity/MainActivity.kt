package com.pspgames.library.activity

import android.Manifest
import android.os.Bundle
import com.pspgames.library.R
import com.pspgames.library.ads.AdmobUtils
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivityMainBinding
import com.pspgames.library.fragment.*
import com.pspgames.library.ads.AdsUtils
import com.pspgames.library.utils.PrefsUtils
import com.pspgames.library.utils.Utils
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.michaelflisar.gdprdialog.GDPR
import com.michaelflisar.gdprdialog.GDPRConsentState
import com.michaelflisar.gdprdialog.GDPRDefinitions
import com.michaelflisar.gdprdialog.GDPRSetup
import com.michaelflisar.gdprdialog.helper.GDPRPreperationData
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems

class MainActivity : BaseActivity<ActivityMainBinding>(), GDPR.IGDPRCallback {
    private val mSetup = GDPRSetup(GDPRDefinitions.ADMOB, GDPRDefinitions.APPLOVIN, GDPRDefinitions.IRONSOURCE, GDPRDefinitions.STARTAPP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TedPermission.create()
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        showGDPRIfNecessary()
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        finish()
                    }
                })
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }

    override fun onConsentNeedsToBeRequested(data: GDPRPreperationData) {
        GDPR.getInstance().showDialog(this, mSetup, data.location)
    }

    override fun onConsentInfoUpdate(consentState: GDPRConsentState?, isNewState: Boolean) {
        AdsUtils.init(this)
        AdsUtils.showBanner(this, binding.adsView)
        if(PrefsUtils.getAdmob().nativeEnable == 1){
            loading.show()
            AdmobUtils.loadNativeAds(this){
                loading.dismiss()
            }
        }
        val adapter = FragmentPagerItemAdapter(
            supportFragmentManager, FragmentPagerItems.with(this)
                .add(getString(R.string.tab_latest), FragmentLatest::class.java)
                .add(getString(R.string.tab_popular), FragmentPopular::class.java)
                .add(getString(R.string.tab_most_download), FragmentMostDownload::class.java)
                .add(getString(R.string.tab_categories), FragmentCategories::class.java)
                .add(getString(R.string.tab_premium), FragmentPremium::class.java)
                .add(getString(R.string.tab_free), FragmentFree::class.java)
                .add(getString(R.string.tab_random), FragmentRandom::class.java)
                .add(getString(R.string.tab_live), FragmentLive::class.java)
                .add(getString(R.string.tab_favourite), FragmentFavourite::class.java)
                .create()
        )
        binding.viewPager.offscreenPageLimit = 0
        binding.viewPager.adapter = adapter
        binding.viewPagerTab.setViewPager(binding.viewPager)
        binding.buttonSearch.setOnClickListener {
            Utils.startAct(this, ActivitySearch::class.java)
        }
        binding.buttonSettings.setOnClickListener {
            Utils.startAct(this, ActivitySettings::class.java)
        }
    }

    private fun showGDPRIfNecessary() {
        GDPR.getInstance().checkIfNeedsToBeShown(this, mSetup)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}