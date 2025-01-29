package com.pspgames.library.ads

import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.FrameLayout
import com.applovin.mediation.*
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkUtils
import com.pspgames.library.App
import com.pspgames.library.utils.PrefsUtils
import com.michaelflisar.gdprdialog.GDPR
import java.util.concurrent.TimeUnit
import kotlin.math.pow

object ApplovinUtils {
    private var consentState = GDPR.getInstance().consentState
    fun init(activity: Activity){
        AppLovinSdk.getInstance( activity ).mediationProvider = "max"
        AppLovinPrivacySettings.setHasUserConsent(consentState.consent.isPersonalConsent, activity)
        loadRewardAds(activity)
        loadInterstitialMax(activity)
    }

    fun loadBannerAds(activity: Activity, view: ViewGroup){
        val adView = MaxAdView(PrefsUtils.getAdmob().applovinBanner, activity)
        val isTablet = AppLovinSdkUtils.isTablet(activity)
        val heightPx = AppLovinSdkUtils.dpToPx(activity, if (isTablet) 90 else 50)
        adView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx)
        adView.setBackgroundColor(Color.WHITE)
        adView.setListener(object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                view.removeAllViews()
                view.addView(adView, 0)
            }

            override fun onAdDisplayed(ad: MaxAd?) {
            }

            override fun onAdHidden(ad: MaxAd?) {
            }

            override fun onAdClicked(ad: MaxAd?) {
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                App.log(error!!.message)
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
            }

            override fun onAdExpanded(ad: MaxAd?) {
            }

            override fun onAdCollapsed(ad: MaxAd?) {
            }
        })
        adView.loadAd()
    }

    private var maxInterstitialAd: MaxInterstitialAd? = null
    private var interstitialCallback: (() -> Unit?)? = null
    private var retryAttemptInterstitial = 0
    private fun loadInterstitialMax(activity: Activity){
        maxInterstitialAd = MaxInterstitialAd(PrefsUtils.getAdmob().applovinInterstitial, activity)
        maxInterstitialAd!!.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                retryAttemptInterstitial = 0
            }

            override fun onAdDisplayed(ad: MaxAd?) {
            }

            override fun onAdHidden(ad: MaxAd?) {
                interstitialCallback?.invoke()
                maxInterstitialAd?.loadAd()
            }

            override fun onAdClicked(ad: MaxAd?) {
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                retryAttemptInterstitial++
                val delayMillis = TimeUnit.SECONDS.toMillis(
                    2.0.pow(6.0.coerceAtMost(retryAttempt))
                        .toLong()
                )

                Handler(Looper.myLooper()!!).postDelayed({ maxInterstitialAd?.loadAd() }, delayMillis)
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                interstitialCallback?.invoke()
                maxInterstitialAd?.loadAd()
            }
        })
        maxInterstitialAd!!.loadAd()
    }

    fun showInterstitial(callback: () -> Unit){
        interstitialCallback = callback
        if (maxInterstitialAd!!.isReady) {
            maxInterstitialAd?.showAd()
        } else {
            interstitialCallback?.invoke()
        }
    }

    private lateinit var rewardedAd: MaxRewardedAd
    private var retryAttempt = 0.0
    private var rewardCallback: (() -> Unit?)? = null
    private fun loadRewardAds(activity: Activity){
        rewardedAd = MaxRewardedAd.getInstance(PrefsUtils.getAdmob().applovinReward, activity)
        rewardedAd.setListener(object : MaxRewardedAdListener{
            override fun onAdLoaded(ad: MaxAd?) {
                retryAttempt = 0.0
            }

            override fun onAdDisplayed(ad: MaxAd?) {
            }

            override fun onAdHidden(ad: MaxAd?) {
                rewardCallback?.invoke()
                rewardedAd.loadAd()
            }

            override fun onAdClicked(ad: MaxAd?) {
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                retryAttempt++
                val delayMillis = TimeUnit.SECONDS.toMillis( 2.0.pow(6.0.coerceAtMost(retryAttempt)).toLong() )
                Handler(Looper.myLooper()!!).postDelayed( { rewardedAd.loadAd() }, delayMillis )
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                rewardedAd.loadAd()
            }

            override fun onRewardedVideoStarted(ad: MaxAd?) {
            }

            override fun onRewardedVideoCompleted(ad: MaxAd?) {

            }

            override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {
            }

        })
        rewardedAd.loadAd()
    }


    fun showRewardAds(callback: () -> Unit){
        rewardCallback = callback
        if (rewardedAd.isReady) {
            rewardedAd.showAd()
        } else {
            rewardCallback?.invoke()
        }
    }
}