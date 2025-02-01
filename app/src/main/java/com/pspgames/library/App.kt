package com.pspgames.library;

import android.app.Application
import android.content.ContextWrapper
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.widget.Toast
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import com.androidnetworking.AndroidNetworking
import com.pspgames.library.database.CollectionTable
import com.pspgames.library.database.FavouriteTable
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.google.firebase.FirebaseApp
import com.michaelflisar.gdprdialog.GDPR
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.Dispatchers


class App : Application(), ImageLoaderFactory{
//    lateinit var workerFactory: WorkerFactory

    companion object {
        lateinit var instance: App
        lateinit var favouriteDatabase: FavouriteTable
        lateinit var collectionTable: CollectionTable
        fun log(any: Any) {
            Log.e("ABENK : ", " $any")
        }

        fun toast(any: Any) {
            Toast.makeText(instance.applicationContext, " $any", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate() {
        super.onCreate()

        WorkManager.initialize(
            this,
            Configuration.Builder().build()
        )
        instance = this
        AndroidNetworking.initialize(applicationContext)
        FirebaseApp.initializeApp(this)
        GDPR.getInstance().init(this)
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(false)
            .build()
        PRDownloader.initialize(applicationContext, config)
        favouriteDatabase = FavouriteTable.getRepo(this)
        collectionTable = CollectionTable.getRepo(this)
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()


    }

//    override fun getWorkManagerConfiguration(): Configuration {
//        return Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()
//    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(false)
            .dispatcher(Dispatchers.Default)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}