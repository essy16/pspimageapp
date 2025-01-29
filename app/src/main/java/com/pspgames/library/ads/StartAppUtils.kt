package com.pspgames.library.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewGroup
import com.pspgames.library.App
import com.pspgames.library.utils.PrefsUtils
import com.michaelflisar.gdprdialog.GDPR
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.adsbase.Ad

import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener


object StartAppUtils {
    private var consentState = GDPR.getInstance().consentState
    @SuppressLint("StaticFieldLeak")
    private lateinit var startAppAd: StartAppAd
    @SuppressLint("StaticFieldLeak")
    private lateinit var rewardStartAppAd: StartAppAd
    fun init(activity: Activity){
        StartAppSDK.init(activity, PrefsUtils.getAdmob().startappId, false)
        StartAppAd.disableSplash()
        StartAppSDK.setUserConsent (activity,
            "pas",
            System.currentTimeMillis(),
            consentState.consent.isPersonalConsent)
        loadInterstitial(activity)
        loadReward(activity)
    }

    fun loadBannerAds(activity: Activity, view: ViewGroup){
        val banner = Banner(activity)
        view.addView(banner, 0)
    }

    private fun loadReward(activity: Activity){
        rewardStartAppAd = StartAppAd(activity)
        rewardStartAppAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener{
            override fun onReceiveAd(p0: Ad) {
                App.log("ads receive")
            }


            override fun onFailedToReceiveAd(p0: Ad?) {
                App.log("ads not receive")
            }
        })
    }

    private fun loadInterstitial(activity: Activity){
        startAppAd = StartAppAd(activity)
        startAppAd.loadAd(object : AdEventListener{
            override fun onReceiveAd(p0: Ad) {
                App.log("ads receive")
            }

            override fun onFailedToReceiveAd(p0: Ad?) {
                App.log("ads not receive")
            }
        })
    }

    fun showInterstitial(callback: () -> Unit){
        if(startAppAd.isReady){
            startAppAd.showAd(object : AdDisplayListener{
                override fun adHidden(p0: Ad?) {
                    callback.invoke()
                }

                override fun adDisplayed(p0: Ad?) {
                }

                override fun adClicked(p0: Ad?) {
                }

                override fun adNotDisplayed(p0: Ad?) {
                }
            })
        } else {
            callback.invoke()
        }
    }

    fun showRewardAds(callback: () -> Unit){
        if(rewardStartAppAd.isReady){
            rewardStartAppAd.showAd(object : AdDisplayListener{
                override fun adHidden(p0: Ad?) {
                    callback.invoke()
                }

                override fun adDisplayed(p0: Ad?) {
                }

                override fun adClicked(p0: Ad?) {
                }

                override fun adNotDisplayed(p0: Ad?) {
                }
            })
        } else {
            callback.invoke()
        }
    }
}