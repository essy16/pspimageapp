package com.pspgames.library.ads

import android.app.Activity
import android.view.ViewGroup
import com.pspgames.library.App
import com.pspgames.library.utils.PrefsUtils
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.*
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener


object YandexUtils {
    fun init(activity: Activity) {
        MobileAds.initialize(activity){ }
        loadInterstitial(activity)
        loadRewardAds(activity)
    }

    fun loadBannerAds(activity: Activity, view: ViewGroup){
        val adView = BannerAdView(activity)
        adView.setAdUnitId(PrefsUtils.getAdmob().yandexBanner)
        adView.setAdSize(AdSize.flexibleSize(728, 90))
        val adRequest: AdRequest = AdRequest.Builder().build()
        adView.setBannerAdEventListener(object : BannerAdEventListener {
            override fun onAdLoaded() {
                view.removeAllViews()
                view.addView(adView, 0)
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                App.log(error.description)
            }

            override fun onAdClicked() {
            }

            override fun onLeftApplication() {
            }

            override fun onReturnedToApplication() {
            }

            override fun onImpression(p0: ImpressionData?) {
            }
        })
        adView.loadAd(adRequest)
    }

    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialCallback: (() -> Unit?)? = null
    private var retryAttemptInterstitial = 0
    private fun loadInterstitial(activity: Activity){
        val adRequest = AdRequest.Builder().build()
        mInterstitialAd = InterstitialAd(activity)
        mInterstitialAd?.setAdUnitId(PrefsUtils.getAdmob().yandexInter)
        mInterstitialAd?.setInterstitialAdEventListener(object : InterstitialAdEventListener{
            override fun onAdLoaded() {
                retryAttemptInterstitial = 0
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                App.log(error.description)
                mInterstitialAd?.destroy()
                mInterstitialAd = null
            }

            override fun onAdDismissed() {
                interstitialCallback?.invoke()
                mInterstitialAd?.loadAd(adRequest)
            }

            override fun onAdShown() {
            }

            override fun onAdClicked() {
            }

            override fun onLeftApplication() {
            }

            override fun onReturnedToApplication() {
            }

            override fun onImpression(p0: ImpressionData?) {
            }
        })
        mInterstitialAd?.loadAd(adRequest)
    }

    fun showInterstitial(callback: () -> Unit){
        interstitialCallback = callback
        if(mInterstitialAd != null && mInterstitialAd?.isLoaded!!){
            mInterstitialAd?.show()
        } else {
            callback.invoke()
        }
    }

    private var rewardedAd: RewardedAd? = null
    private var rewardCallback: (() -> Unit?)? = null
    private fun loadRewardAds(activity: Activity){
        val adRequest: AdRequest = AdRequest.Builder().build()
        rewardedAd = RewardedAd(activity)
        rewardedAd?.let {
            it.setAdUnitId(PrefsUtils.getAdmob().yandexReward)
            it.setRewardedAdEventListener(object : RewardedAdEventListener {
                override fun onAdLoaded() {

                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    App.log(error.description)
                    it.destroy()
                    rewardedAd = null
                    rewardCallback?.invoke()
                }

                override fun onAdShown() {

                }

                override fun onAdDismissed() {

                }

                override fun onRewarded(p0: Reward) {
                    rewardCallback?.invoke()
                    it.loadAd(adRequest)
                }

                override fun onAdClicked() {

                }

                override fun onLeftApplication() {

                }

                override fun onReturnedToApplication() {

                }

                override fun onImpression(p0: ImpressionData?) {

                }
            })
            it.loadAd(adRequest)
        }
    }

    fun showRewardAds(callback: () -> Unit) {
        rewardCallback = callback
        if(rewardedAd != null && rewardedAd?.isLoaded!!){
            rewardedAd?.show()
        } else {
            callback.invoke()
        }
    }
}