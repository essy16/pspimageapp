package com.pspgames.library.dialog

import android.app.Activity
import android.app.Dialog
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.LayoutInflater
import android.view.Window
import androidx.annotation.RequiresApi
import com.pspgames.library.App
import com.pspgames.library.R
import com.pspgames.library.base.handlerDelayed
import com.pspgames.library.databinding.DialogSetWallpaperBinding
import com.pspgames.library.enums.AdsProvider

import com.pspgames.library.ads.AdsUtils
import com.pspgames.library.utils.PrefsUtils


@RequiresApi(Build.VERSION_CODES.N)
class DialogSetWallpaper(context: Context, bitmap: Bitmap, callback: () -> Unit): Dialog(context, R.style.AlertDialog) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val binding :DialogSetWallpaperBinding = DialogSetWallpaperBinding .inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        val wallpaperManager = WallpaperManager.getInstance(context)
        val dialogProgress = DialogProgress(context)
        binding.buttonHomeScreen.setOnClickListener {
            try {
                dismiss()
                dialogProgress.show()
                handlerDelayed(1000){
                    if(PrefsUtils.getAdmob().provider != AdsProvider.DISABLE.name){
                        AdsUtils.showInterstitial(context as Activity){
                            try {
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                                App.toast(context.getString(R.string.wallpaper_set_success))
                                dialogProgress.dismiss()
                                callback.invoke()
                            } catch (e: Exception) {
                                App.toast(context.getString(R.string.wallpaper_set_failed))
                                dialogProgress.dismiss()
                            }
                        }
                    } else {
                        try {
                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                            App.toast(context.getString(R.string.wallpaper_set_success))
                            dialogProgress.dismiss()
                            callback.invoke()
                        } catch (e: Exception) {
                            App.toast(context.getString(R.string.wallpaper_set_failed))
                            dialogProgress.dismiss()
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
        binding.buttonLockScreen.setOnClickListener {
            try {
                dismiss()
                dialogProgress.show()
                handlerDelayed(1000){
                    if(PrefsUtils.getAdmob().provider != AdsProvider.DISABLE.name) {
                        AdsUtils.showInterstitial(context as Activity) {
                            try {
                                wallpaperManager.setBitmap(
                                    bitmap,
                                    null,
                                    true,
                                    WallpaperManager.FLAG_LOCK
                                )
                                App.toast(context.getString(R.string.wallpaper_set_success))
                                dialogProgress.dismiss()
                                callback.invoke()
                            } catch (e: Exception) {
                                App.toast(context.getString(R.string.wallpaper_set_failed))
                                dialogProgress.dismiss()
                            }
                        }
                    } else {
                        try {
                            wallpaperManager.setBitmap(
                                bitmap,
                                null,
                                true,
                                WallpaperManager.FLAG_LOCK
                            )
                            App.toast(context.getString(R.string.wallpaper_set_success))
                            dialogProgress.dismiss()
                            callback.invoke()
                        } catch (e: Exception) {
                            App.toast(context.getString(R.string.wallpaper_set_failed))
                            dialogProgress.dismiss()
                        }
                    }
                }
            } catch (_: Exception) {

            }
        }
        binding.buttonBothScren.setOnClickListener {
            try {
                dismiss()
                dialogProgress.show()
                handlerDelayed(1000){
                    if(PrefsUtils.getAdmob().provider != AdsProvider.DISABLE.name) {
                        AdsUtils.showInterstitial(context as Activity) {
                            try {
                                wallpaperManager.setBitmap(bitmap)
                                App.toast(context.getString(R.string.wallpaper_set_success))
                                dialogProgress.dismiss()
                                callback.invoke()
                            } catch (e: Exception) {
                                App.toast(context.getString(R.string.wallpaper_set_failed))
                                dialogProgress.dismiss()
                            }
                        }
                    } else {
                        try {
                            wallpaperManager.setBitmap(bitmap)
                            App.toast(context.getString(R.string.wallpaper_set_success))
                            dialogProgress.dismiss()
                            callback.invoke()
                        } catch (e: Exception) {
                            App.toast(context.getString(R.string.wallpaper_set_failed))
                            dialogProgress.dismiss()
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}