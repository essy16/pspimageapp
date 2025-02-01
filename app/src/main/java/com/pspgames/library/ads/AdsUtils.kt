package com.pspgames.library.ads

import android.app.Activity
import android.content.Context
import android.media.tv.AdRequest
import android.view.ViewGroup
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.pspgames.library.R
import com.pspgames.library.dialog.BaseDialog
import com.pspgames.library.enums.AdsProvider
import com.pspgames.library.utils.PrefsUtils

object AdsUtils {
    fun init(activity: Activity){
        when (PrefsUtils.getAdmob().provider) {
            AdsProvider.ADMOB.name -> {
                AdmobUtils.init(activity)
            }
            AdsProvider.FACEBOOKBIDDING.name -> {
                AdmobUtils.init(activity)
            }
            AdsProvider.APPLOVIN.name -> {
                ApplovinUtils.init(activity)
            }
            AdsProvider.STARTAPP.name -> {
                StartAppUtils.init(activity)
            }
            AdsProvider.YANDEX.name -> {
                YandexUtils.init(activity)
            }
        }
    }

    fun showInterstitial(activity: Activity, callback: () -> Unit){
        when (PrefsUtils.getAdmob().provider) {
            AdsProvider.ADMOB.name -> {
                AdmobUtils.showInterstitial(activity, callback)
            }
            AdsProvider.FACEBOOKBIDDING.name -> {
                AdmobUtils.showInterstitial(activity, callback)
            }
            AdsProvider.APPLOVIN.name -> {
                ApplovinUtils.showInterstitial(callback)
            }
            AdsProvider.STARTAPP.name -> {
                StartAppUtils.showInterstitial(callback)
            }
            AdsProvider.YANDEX.name -> {
                YandexUtils.showInterstitial(callback)
            }
            else -> {
                callback.invoke()
            }
        }
    }


    fun showReward(activity: Activity, callback: () -> Unit){
        if(PrefsUtils.getAdmob().provider == AdsProvider.DISABLE.name){
            callback.invoke()
        } else {
            if(PrefsUtils.getAdmob().rewardEnable == 1){
                val dialogAds = BaseDialog(activity)
                dialogAds.setCancelable(true)
                dialogAds.setTitle(activity.getString(R.string.dialog_reward_title))
                dialogAds.setDescrtiption(activity.getString(R.string.dialog_reward_description))
                dialogAds.setPositiveButton(activity.getString(R.string.dialog_reward_button1)){
                    dialogAds.dismiss()
                    when (PrefsUtils.getAdmob().provider) {
                        AdsProvider.ADMOB.name -> {
                            AdmobUtils.showRewardAds(activity, callback)
                        }
                        AdsProvider.FACEBOOKBIDDING.name -> {
                            AdmobUtils.showRewardAds(activity, callback)
                        }
                        AdsProvider.APPLOVIN.name -> {
                            ApplovinUtils.showRewardAds(callback)
                        }
                        AdsProvider.STARTAPP.name -> {
                            StartAppUtils.showRewardAds(callback)
                        }
                        AdsProvider.YANDEX.name -> {
                            YandexUtils.showRewardAds(callback)
                        }
                    }
                }
                dialogAds.setNegativeButton(activity.getString(R.string.dialog_reward_button2)){
                    dialogAds.dismiss()
                }
                dialogAds.show()
            } else {
                callback.invoke()
            }
        }
    }

    fun showBanner(activity: Activity, view: ViewGroup){
        if(PrefsUtils.getAdmob().interstitialEnable == 1){
            when (PrefsUtils.getAdmob().provider) {
                AdsProvider.ADMOB.name -> {
                    AdmobUtils.loadBannerAds(activity, view)
                }
                AdsProvider.FACEBOOKBIDDING.name -> {
                    AdmobUtils.loadBannerAds(activity, view)
                }
                AdsProvider.APPLOVIN.name -> {
                    ApplovinUtils.loadBannerAds(activity, view)
                }
                AdsProvider.STARTAPP.name -> {
                    StartAppUtils.loadBannerAds(activity, view)
                }
                AdsProvider.YANDEX.name -> {
                    YandexUtils.loadBannerAds(activity, view)
                }
            }
        }
    }
}