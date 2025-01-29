package com.pspgames.library.downloader

import okhttp3.Interceptor
import okhttp3.Response

class DownloadInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var tryCount = 0
        val maxTries = 3
        val request = chain.request()
        var response = chain.proceed(request)
        while (!response.isSuccessful && tryCount < maxTries){
            tryCount++
            response.close()
            response = chain.proceed(request)
        }
        return response
    }
}