package com.pspgames.library.network

import com.pspgames.library.Config
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitBuilder {
    fun build() : ApiServices{
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(interceptor)
            .hostnameVerifier { _, _ -> true }
            .build()
        return Retrofit.Builder()
            .baseUrl(Config.SERVER_URL + "/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiServices::class.java)
    }
}