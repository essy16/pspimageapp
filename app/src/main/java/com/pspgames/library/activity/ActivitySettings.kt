package com.pspgames.library.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.pspgames.library.App
import com.pspgames.library.Config
import com.pspgames.library.R
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivitySettingsBinding
import com.pspgames.library.dialog.DialogClearCache
import com.pspgames.library.dialog.DialogTheme
import com.pspgames.library.enums.ThemeMode
import com.pspgames.library.enums.WallpaperTarget
import com.pspgames.library.model.ModelSettings
import com.pspgames.library.network.Status
import com.pspgames.library.utils.Utils
import com.pspgames.library.utils.WorkerHelper
import com.jakewharton.processphoenix.ProcessPhoenix
import com.onesignal.OneSignal
import com.pixplicity.easyprefs.library.Prefs


class ActivitySettings : BaseActivity<ActivitySettingsBinding>() {
    private var targetCounting = 0
    private val listTarget = listOf(
        WallpaperTarget.HOME.name,
        WallpaperTarget.LOCK.name,
        WallpaperTarget.BOTH.name
    )
    private var intervalCounting = 0
    private val listInterval = listOf(
        15,
        30,
        60,
        180,
        360,
        720,
        1440
    )
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.toolbar.title = "Settings"
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        loading.show()
        getSettings { modelSettings ->
            binding.let {
                calculateCache()
                it.buttonClear.setOnClickListener {
                    DialogClearCache(this, onClear = {
                        Utils.clearImageCache(this)
                        calculateCache()
                    }).show()
                }
                it.buttonPrivacy.setOnClickListener {
                    try {
                        val uri: Uri = Uri.parse(modelSettings.privacy)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                    } catch (_: Exception) {
                    }
                }
                it.buttonMore.setOnClickListener {
                    val uri: Uri = Uri.parse(modelSettings.packagename)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
                it.buttonRate.setOnClickListener {
                    val uri: Uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
                it.backgroundDesc.setOnClickListener { _ ->
                    it.switchBackground.isChecked = !it.switchBackground.isChecked
                    Prefs.putBoolean("blurred_background", it.switchBackground.isChecked)
                }
                it.switchBackground.isChecked = Prefs.getBoolean("blurred_background", false)
                it.switchBackground.setOnCheckedChangeListener { _, b ->
                    Prefs.putBoolean("blurred_background", b)
                }
                it.switchNotification.isChecked = Prefs.getBoolean("enable_notification", true)
                it.switchNotification.setOnCheckedChangeListener { _, b ->
                    Prefs.putBoolean("enable_notification", b)
                    OneSignal.disablePush(b)
                }
                it.buttonInterval.visibility = if(Prefs.getBoolean("auto_wallpaper", false)) View.VISIBLE else View.GONE
                it.buttonSetAs.visibility = if(Prefs.getBoolean("auto_wallpaper", false)) View.VISIBLE else View.GONE
                it.buttonCollection.visibility = if(Prefs.getBoolean("auto_wallpaper", false)) View.VISIBLE else View.GONE
                it.switchAutoWallpaper.isChecked = Prefs.getBoolean("auto_wallpaper", false) && App.collectionTable.toList().size != 0
                it.switchAutoWallpaper.setOnCheckedChangeListener { _, isChecked ->
                    if(App.collectionTable.toList().size == 0){
                        App.toast(getString(R.string.auto_wallpaper_error))
                        it.switchAutoWallpaper.isChecked = false
                        return@setOnCheckedChangeListener
                    }
                    it.buttonInterval.visibility = if(isChecked) View.VISIBLE else View.GONE
                    it.buttonSetAs.visibility = if(isChecked) View.VISIBLE else View.GONE
                    it.buttonCollection.visibility = if(isChecked) View.VISIBLE else View.GONE
                    Prefs.putBoolean("auto_wallpaper", isChecked)
                    if(isChecked){
                        WorkerHelper.enqueue(this, true)
                    }
                }

                if(Prefs.contains("auto_wallpaper_interval")){
                    Prefs.getInt("auto_wallpaper_interval", 15).let {interval ->
                        if(interval < 60){
                            binding.textInterval.text = "$interval min"

                        } else {
                            binding.textInterval.text =  (interval / 60).toString() + " hour"
                        }
                    }
                } else {
                    Prefs.putInt("auto_wallpaper_interval", 15)
                    binding.textInterval.text = "15 min"
                }

                it.buttonInterval.setOnClickListener {
                    val interval = if(intervalCounting < (listInterval.size - 1)){
                        intervalCounting++
                         listInterval[intervalCounting]
                    } else {
                        intervalCounting = 0
                        listInterval[0]
                    }
                    val intervalText = if(interval < 60){
                        "$interval min"
                    } else {
                        (interval / 60).toString() + " hour"
                    }
                    binding.textInterval.text = intervalText
                    Prefs.putInt("auto_wallpaper_interval", interval)
                }

                if(Prefs.contains("auto_wallpaper_target")){
                    binding.textSetAs.text = Prefs.getString("auto_wallpaper_target")
                } else {
                    Prefs.putString("auto_wallpaper_target", WallpaperTarget.HOME.name)
                    binding.textSetAs.text = WallpaperTarget.HOME.name
                }
                it.buttonSetAs.setOnClickListener {
                    val target = if(targetCounting < (listTarget.size - 1)){
                        targetCounting++
                        listTarget[targetCounting]
                    } else {
                        targetCounting = 0
                        listTarget[0]
                    }
                    binding.textSetAs.text = target
                    Prefs.putString("auto_wallpaper_target", target)
                }
                it.buttonCollection.setOnClickListener {
                    startActivity(Intent(this, ActivityCollection::class.java))
                }
                if(Prefs.contains("theme_mode")){
                    binding.textTheme.text = Prefs.getString("theme_mode")
                } else {
                    Prefs.putString("theme_mode", ThemeMode.DARK.name)
                }
                it.buttonTheme.setOnClickListener {
                    if(Prefs.getString("theme_mode") == ThemeMode.DARK.name){
                        Prefs.putString("theme_mode", ThemeMode.LIGHT.name)
                        binding.textTheme.text = ThemeMode.LIGHT.name
                        DialogTheme(this, onRestart = {
                            if(Config.UI_VERSION == 2){
                                ProcessPhoenix.triggerRebirth(this, Intent(this, MainActivity2::class.java))
                            } else {
                                ProcessPhoenix.triggerRebirth(this, Intent(this, MainActivity::class.java))
                            }
                        }).show()
                    } else {
                        Prefs.putString("theme_mode", ThemeMode.DARK.name)
                        binding.textTheme.text = ThemeMode.DARK.name
                        DialogTheme(this, onRestart = {
                            if(Config.UI_VERSION == 2){
                                ProcessPhoenix.triggerRebirth(this, Intent(this, MainActivity2::class.java))
                            } else {
                                ProcessPhoenix.triggerRebirth(this, Intent(this, MainActivity::class.java))
                            }
                        }).show()
                    }
                }
            }
        }
        loading.dismiss()
    }

    private fun calculateCache(){
        val cacheSize = Utils.getImageCacheSize(this)
        binding.cacheDesk.text = String.format(getString(R.string.settings_clear_desc), cacheSize)
    }

    private fun getSettings(callback: (ModelSettings) -> Unit) {
        viewModel.getSettings().observe(this) { resource ->
            if (resource?.status == Status.LOADING) {
                App.log("loading")
            } else {
                resource.data?.let {
                    callback.invoke(it)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}