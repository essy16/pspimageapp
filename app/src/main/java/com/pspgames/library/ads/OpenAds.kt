package com.pspgames.library.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.*

class OpenAds (private var application: Application, private var openId: String, private var blockedActivity: ArrayList<Class<*>>, var activity: Activity? = null, var callback: (() -> Unit)? = null) :
    LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null
    private var isShowingAd = false
    private var loadTime: Long = 0
    private var showTime: Long = 0
    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        if(callback != null){
            showForce()
        }
    }

    private fun showForce(){
        callback?.invoke()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        showAdIfAvailable()
    }

    private fun showAdIfAvailable() {
        if(!isShowingAd && isAdAvailable() && isNotBlockedActivity()) {
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    showTime = Date().time
                    fetchAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true

                }
            }
            appOpenAd !!.show(currentActivity !!)
        } else {
            fetchAd()
        }
    }

    fun fetchAd() {
        if(isAdAvailable()) {
            return
        }
        AppOpenAd.load(application, openId, getAdRequest(), 1, object :
            AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                super.onAdLoaded(ad)
                appOpenAd = ad
                loadTime = Date().time
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
            }
        })
    }


    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * 4
    }
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo()
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    private fun isNotBlockedActivity() : Boolean{
        return !blockedActivity.contains(currentActivity!!::class.java)
    }
}