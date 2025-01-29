package com.pspgames.library.network

import com.pspgames.library.Config
import com.pspgames.library.model.ModelAdmob
import com.pspgames.library.model.ModelCategories
import com.pspgames.library.model.ModelColor
import com.pspgames.library.model.ModelConsoleCategory
import com.pspgames.library.model.ModelConsoleResult
import com.pspgames.library.model.ModelGenre
import com.pspgames.library.model.ModelHome
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.model.ModelSettings
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url


interface ApiServices {
    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("settings")
    suspend fun getSettings(): ModelSettings

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("admob")
    suspend fun getAdmob(): ModelAdmob

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("home")
    suspend fun getHome(): ArrayList<ModelHome>

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("color")
    suspend fun getColor(): ArrayList<ModelColor>

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("add/show/wallpaper")
    suspend fun addView(@QueryMap params : Map<String, String>)

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("add/download/wallpaper/")
    suspend fun addDownload(@QueryMap params : Map<String, String>)

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("wallpaper/search")
    suspend fun getSearch(@QueryMap params : Map<String, String>): ModelLatest

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("wallpaper/cid")
    suspend fun getList(@QueryMap params : Map<String, String>): ModelLatest

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("wallpaper/color")
    suspend fun getByColor(@QueryMap params : Map<String, String>): ModelLatest

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("single")
    suspend fun getSingle(@QueryMap params : Map<String, String>): ModelLatest.Data

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("categories")
    suspend fun getCategories(@QueryMap params : Map<String, String>): ModelCategories

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("wallpaper/premium")
    suspend fun getPremiumOrFree(@QueryMap params : Map<String, String>): ModelLatest

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("wallpaper")
    suspend fun getLatest(@QueryMap params : Map<String, String>) : ModelLatest

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("wallpaper/live")
    suspend fun getLive(@QueryMap params : Map<String, String>) : ModelLatest

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("wallpaper/double")
    suspend fun getDouble(@QueryMap params : Map<String, String>) : ModelLatest

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("wallpaper/random")
    suspend fun getRandom(@QueryMap params : Map<String, String>) : ModelLatest

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("wallpaper/download")
    suspend fun getMostDownloaded(@QueryMap params : Map<String, String>) : ModelLatest

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("wallpaper/popular")
    suspend fun getPopular(@QueryMap params : Map<String, String>) : ModelLatest

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("console/cat")
    suspend fun getConsoleCategory() : ArrayList<ModelConsoleCategory>

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("console/search")
    suspend fun getConsoleByQuery(@Query("query") query: String, @Query("page") page: Int) : ModelConsoleResult

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("console/cid")
    suspend fun getConsoleByCid(@Query("id") cid: String, @Query("page") page: Int) : ModelConsoleResult

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("console/genre")
    suspend fun getConsoleByGenre(@Query("genre") genre: String, @Query("page") page: Int) : ModelConsoleResult

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("console/latest")
    suspend fun getConsoleLatest(@Query("page") page: Int, @Query("cid") cid: String, @Query("genre") genre: String) : ModelConsoleResult

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("console/popular")
    suspend fun getConsolePopular(@Query("page") page: Int, @Query("cid") cid: String, @Query("genre") genre: String) : ModelConsoleResult

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("console/updated")
    suspend fun getConsoleUpdated(@Query("page") page: Int, @Query("cid") cid: String, @Query("genre") genre: String) : ModelConsoleResult

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("console/rated")
    suspend fun getConsoleRated(@Query("page") page: Int, @Query("cid") cid: String, @Query("genre") genre: String) : ModelConsoleResult

    @Headers("Server-Key: " + Config.SERVER_KEY)
    @GET("genre")
    suspend fun getGenre() : ArrayList<ModelGenre>

    @GET
    fun downloadFile(@Url fileUrl: String): Call<ResponseBody>
}