package com.pspgames.library.downloader

import com.downloader.Response
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadServices {
    @GET
    @Streaming
    suspend fun downloadFile(@Url url: String): ResponseBody

    @GET
    @Streaming
    fun downloadFileWithRange(@Url url: String, @Header("Range") range: String): ResponseBody

    @HEAD
    suspend fun getFileHeaders(@Url url: String): Response
}