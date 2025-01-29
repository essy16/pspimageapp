package com.pspgames.library

import com.pspgames.library.activity.*
import com.pspgames.library.enums.Wallpaper

object Config {
    const val SERVER_URL = "https://adminpsp.gameslibrary.pk"
    const val SERVER_KEY = "XjjXvKKAxjYmJjjOdFSKdAOlZwTkvlQrXRShNQlIzRedUzPifp"
    val blockedActivityFromOpenAds = arrayListOf<Class<*>>(ActivityDetail::class.java, ActivitySettings::class.java, ActivitySetWallpaper::class.java, ActivitySplash::class.java, MainActivity::class.java)

    //for thumbnail
    const val ENABLE_THUMBNAIL_INSTEAD_ORIGINAL_IMAGE = false // if wallpaper not loaded you an disable it by changing true to false

    //for ui version 2
    const val UI_VERSION = 2 // YOU CAN CHANGE UI VERSION TO 1 OR 2
    val HOME_BANNER = Wallpaper.popular // you can change to latest, random etc. check enum/Wallpaper to find variable\

    // Native ads on detail, only admob support
    const val NATIVE_DETAIL_SHOW_AFTER = 10 //
    const val NATIVE_ADS_VARIATION = 3 //

    const val ENABLE_HOME_BANNER = true
    const val ENABLE_BOARDING_PAGE = true
}