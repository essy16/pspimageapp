package com.pspgames.library.downloader

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object DownloadClient {
    fun createService() : DownloadServices {
        val client = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .addInterceptor(DownloadInterceptor())
            .build()

        return Retrofit.Builder()
            .baseUrl("https://google.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DownloadServices::class.java)
    }
}