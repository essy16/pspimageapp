package com.pspgames.library.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private val apiService: ApiServices) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(MainViewModel::class.java)){
            return MainViewModel(apiService)as T
        }

        throw IllegalArgumentException("Unknown class name")
    }
}