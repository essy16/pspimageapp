package com.pspgames.library.model


import com.google.gson.annotations.SerializedName

data class ModelAdmob(
    @SerializedName("admob_banner")
    val admobBanner: String,
    @SerializedName("admob_interstitial")
    val admobInterstitial: String,
    @SerializedName("admob_open")
    val admobOpen: String,
    @SerializedName("admob_reward")
    val admobReward: String,
    @SerializedName("applovin_banner")
    val applovinBanner: String,
    @SerializedName("applovin_interstitial")
    val applovinInterstitial: String,
    @SerializedName("applovin_reward")
    val applovinReward: String,
    @SerializedName("banner_enable")
    val bannerEnable: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("interstitial_enable")
    val interstitialEnable: Int,
    @SerializedName("ironsource_id")
    val ironsourceId: String,
    @SerializedName("open_enable")
    val openEnable: Int,
    @SerializedName("provider")
    var provider: String,
    @SerializedName("reward_enable")
    val rewardEnable: Int,
    @SerializedName("startapp_id")
    val startappId: String,
    @SerializedName("admob_native")
    val admobNative: String,
    @SerializedName("native_enable")
    val nativeEnable: Int,
    @SerializedName("yandex_banner")
    val yandexBanner: String,
    @SerializedName("yandex_inter")
    val yandexInter: String,
    @SerializedName("yandex_reward")
    val yandexReward: String
)