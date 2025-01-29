package com.pspgames.library.activity

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.WindowManager
import com.pspgames.library.App
import com.pspgames.library.Config
import com.pspgames.library.ads.OpenAds
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivitySplashBinding
import com.pspgames.library.enums.AdsProvider
import com.pspgames.library.model.ModelAdmob
import com.pspgames.library.model.ModelSettings
import com.pspgames.library.network.Status
import com.pspgames.library.utils.PrefsUtils
import com.pspgames.library.utils.Utils
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.onesignal.OneSignal
import com.pixplicity.easyprefs.library.Prefs


class ActivitySplash : BaseActivity<ActivitySplashBinding>() {
    private var id = ""
    private var splashOpened = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        getSettings { modelSettings ->
            OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
            OneSignal.initWithContext(this)
            OneSignal.setAppId(modelSettings.onesignal_id)
            //OneSignal.promptForPushNotifications()
            OneSignal.disablePush(Prefs.getBoolean("enable_notification", true))
            OneSignal.setNotificationWillShowInForegroundHandler {
                val notification = it.notification
                it.complete(notification)
            }
            OneSignal.setNotificationOpenedHandler { result ->
                val data = result.notification.additionalData
                if(data != null){
                    val json : JsonObject = JsonParser.parseString(result.notification.additionalData.toString()).asJsonObject
                    try {
                        id = json.get("id").asString
                        if(splashOpened){
                            val bundle = Bundle()
                            bundle.putString("id", id)
                            Utils.startAct(this, ActivitySingleWallpaper::class.java, bundle)
                        }
                    } catch (e: Exception) {
                    }
                }
            }
            getAdmob { modelAdmob ->
                PrefsUtils.saveAdmob(modelAdmob)
                if (modelAdmob.openEnable == 1 && modelAdmob.provider == AdsProvider.ADMOB.name) {
                    OpenAds(App.instance, modelAdmob.admobOpen, Config.blockedActivityFromOpenAds, this) {
                        if(id != ""){
                            val bundle = Bundle()
                            bundle.putString("id", id)
                            Utils.startAct(this, ActivitySingleWallpaper::class.java, bundle)
                        } else {
                            startUi()
                        }
                    }
                } else {
                    if(id != ""){
                        val bundle = Bundle()
                        bundle.putString("id", id)
                        Utils.startAct(this, ActivitySingleWallpaper::class.java, bundle)
                    } else {
                        startUi()
                    }
                }
                splashOpened = true
            }

        }
    }

    private fun startUi(){
        if(Config.ENABLE_BOARDING_PAGE){
            if(!PrefsUtils.getFirst()){
                Utils.startAct(this, ActivityWelcome::class.java)
                finish()
            } else {
                if(Config.UI_VERSION == 1){
                    Utils.startAct(this, MainActivity::class.java)
                    finish()
                } else {
                    Utils.startAct(this, MainActivity2::class.java)
                    finish()
                }
            }
        } else {
            if(Config.UI_VERSION == 1){
                Utils.startAct(this, MainActivity::class.java)
                finish()
            } else {
                Utils.startAct(this, MainActivity2::class.java)
                finish()
            }
        }
    }

    private fun getSettings(callback: (ModelSettings) -> Unit) {
        viewModel.getSettings().observe(this, { resource ->
            if (resource?.status == Status.LOADING) {
               // App.log("loading")
            } else {
                resource.data?.let {
                    callback.invoke(it)
                }
            }
        })
    }

    private fun getAdmob(callback: (ModelAdmob) -> Unit) {
        viewModel.getAdmob().observe(this, { resource ->
            if (resource?.status == Status.LOADING) {
                //App.log("loading")
            } else {
                resource.data?.let {
                    callback.invoke(it)
                }
            }
        })
    }
}