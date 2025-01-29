package com.pspgames.library.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.pspgames.library.R
import com.pspgames.library.dialog.DialogProgress
import com.pspgames.library.enums.ThemeMode
import com.pspgames.library.network.MainViewModel
import com.pspgames.library.network.RetrofitBuilder
import com.pspgames.library.network.ViewModelFactory
import com.pixplicity.easyprefs.library.Prefs

abstract class BaseActivity<V : ViewBinding> : AppCompatActivity() {
    lateinit var binding: V
    lateinit var viewModel: MainViewModel
    lateinit var loading: DialogProgress
    override fun onCreate(savedInstanceState: Bundle?) {
        beforeOnCreate()
        super.onCreate(savedInstanceState)
        if(Prefs.contains("theme_mode")){
            val themeMode = Prefs.getString("theme_mode")
            if(themeMode == ThemeMode.DARK.name){
                setTheme(ThemeMode.DARK)
            } else {
                setTheme(ThemeMode.LIGHT)
            }
        } else {
            setTheme(R.style.DarkModeTheme)
        }
        binding = getBinding()
        setupViewModel()
        setContentView(binding.root)
        setupBinding(binding)
        loading = DialogProgress(this)
    }
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this,  ViewModelFactory(RetrofitBuilder.build())).get(MainViewModel::class.java)
    }
    open fun setupBinding(binding: V){}
    open fun beforeOnCreate(){}
    fun setTheme(themeMode: ThemeMode){
        if(themeMode == ThemeMode.DARK){
            setTheme(R.style.DarkModeTheme)
        } else {
            setTheme(R.style.LightModeTheme)
        }
    }
}
