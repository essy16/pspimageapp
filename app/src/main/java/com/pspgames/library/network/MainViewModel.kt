package com.pspgames.library.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.pspgames.library.App
import com.pspgames.library.utils.logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val apiService: ApiServices) : ViewModel() {

    fun getSettings() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getSettings()))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getAdmob() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getAdmob()))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getHome() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getHome()))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
        }
    }

    fun getColor() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getColor()))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
        }
    }

    fun getCategories(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getCategories(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
        }
    }

    fun getPremiumOrFree(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getPremiumOrFree(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getLatest(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getLatest(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getLive(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getLive(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getDouble(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getDouble(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getRandom(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getRandom(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getMostDownloaded(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getMostDownloaded(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getPopular(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getPopular(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getSearch(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getSearch(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getList(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getList(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getSingle(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getSingle(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getByColor(params : Map<String, String>) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getByColor(params)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getConsoleCategory() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getConsoleCategory()))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getConsoleByQuery(page: Int, query: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getConsoleByQuery(query, page)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getConsoleByCid(page: Int, cid: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getConsoleByCid(cid, page)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getConsoleByGenre(page: Int,genre: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getConsoleByGenre(genre, page)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getConsoleLatest(page: Int, cid: String, genre: String) = liveData(Dispatchers.IO) {

        logging(cid)
        logging(genre)
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getConsoleLatest(page, cid, genre)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getConsolePopular(page: Int, cid: String, genre: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getConsolePopular(page, cid, genre)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getConsoleUpdated(page: Int, cid: String, genre: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getConsoleUpdated(page, cid, genre)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getConsoleRated(page: Int ,cid: String, genre: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getConsoleRated(page, cid, genre)))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun getGenre() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data=apiService.getGenre()))
        }catch (exception: Exception){
            emit(Resource.error(data = null))
            App.log(exception.message!!)
        }
    }

    fun addView(params : Map<String, String>){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.addView(params)
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    App.toast(e.message!!)
                }
            }
        }
    }

    fun addDownload(params : Map<String, String>){
        CoroutineScope(Dispatchers.IO).launch {
            apiService.addDownload(params)
        }
    }
}