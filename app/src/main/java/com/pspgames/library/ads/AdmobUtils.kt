package com.pspgames.library.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import com.pspgames.library.Config
import com.pspgames.library.App
import com.pspgames.library.utils.PrefsUtils
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.michaelflisar.gdprdialog.GDPR
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.nativead.NativeAd
import kotlin.random.Random

@Suppress("DEPRECATION")
object AdmobUtils {
    private var mRewardedAd: RewardedAd? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var consentState = GDPR.getInstance().consentState
    private val listNative: ArrayList<NativeAd> = arrayListOf()
    private val request = if(consentState.consent.isPersonalConsent){
        val extras = Bundle()
        extras.putString("npa", "1")
        AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
    } else {
        AdRequest.Builder().build()
    }

    fun init(activity: Activity){
        loadRewardAds(activity)
        loadInterstitial(activity)
    }

    @SuppressLint("StaticFieldLeak")
    private var adLoader: AdLoader? = null
    @Suppress("UnusedEquals")
    fun loadNativeAds(activity: Activity, callback: () -> Unit){
        adLoader = AdLoader.Builder(activity, PrefsUtils.getAdmob().admobNative)
            .forNativeAd { nativeAd : NativeAd ->
                if (adLoader?.isLoading!!) {
                    listNative.add(nativeAd)
                } else {
                    adLoader = null
                    callback.invoke()
                }
            }
            .withAdListener(object : AdListener(){
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    callback.invoke()
                }
            })
            .build()
        adLoader?.loadAds(AdRequest.Builder().build(), Config.NATIVE_ADS_VARIATION + 1)
    }

    fun nativeIsLoaded() : Boolean{
        return listNative.isNotEmpty()
    }

    fun getNative(): NativeAd{
        return listNative[Random.nextInt(listNative.size - 1)]
    }

    fun loadRewardAds(activity: Activity) {
        RewardedAd.load(
            activity,
            PrefsUtils.getAdmob().admobReward,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    loadInterstitial(activity)
                    mRewardedAd = null
                    App.log(adError.message)
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                    App.log("is reward loaded")
                }
            })
    }

    fun showRewardAdsWithBackup(activity: Activity,callback: () -> Unit) {
        var rewardSuccess = false
        if (mRewardedAd != null) {
            mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    showInterstitial(activity, callback)
                    loadRewardAds(activity)
                }

                override fun onAdDismissedFullScreenContent() {
                    if(rewardSuccess){
                        callback.invoke()
                    }
                    loadRewardAds(activity)
                }
            }
            mRewardedAd?.show(activity) {
                rewardSuccess = true
            }
        } else {
            showInterstitial(activity, callback)
        }
    }


    fun showRewardAds(activity: Activity,callback: () -> Unit) {
        var rewardSuccess = false
        if (mRewardedAd != null) {
            mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    callback.invoke()
                    loadRewardAds(activity)
                }

                override fun onAdDismissedFullScreenContent() {
                    if(rewardSuccess){
                        callback.invoke()
                    }
                    loadRewardAds(activity)
                }
            }
            mRewardedAd?.show(activity) {
                rewardSuccess = true
            }
        } else {
            showInterstitial(activity, callback)
        }
    }

    fun loadBannerAds(activity: Activity, view: ViewGroup) {
        val adView = AdView(activity)
        adView.adUnitId = PrefsUtils.getAdmob().admobBanner
        adView.setAdSize(adSize(activity, view))
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() { // Code to be executed when an ad finishes loading.
                view.addView(adView, 0)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) { // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() { // Code to be executed when an ad opens an overlay that
            }

            override fun onAdClicked() { // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() { // Code to be executed when the user is about to return
            }
        }
        adView.loadAd(request)
    }

    fun loadInterstitial(activity: Activity) {
        InterstitialAd.load(
            activity,
            PrefsUtils.getAdmob().admobInterstitial,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })
    }

     fun showInterstitial(activity: Activity, callback: () -> Unit) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    callback.invoke()
                    loadInterstitial(activity)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    callback.invoke()
                    loadInterstitial(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    mInterstitialAd = null
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            callback.invoke()
        }
    }

    private fun adSize(activity: Activity, view: ViewGroup): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val density = outMetrics.density
        var adWidthPixels = view.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

}