package com.pspgames.library.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import com.pspgames.library.Config
import com.pspgames.library.R
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivityAllowPermissionBinding
import com.pspgames.library.utils.PrefsUtils
import com.pspgames.library.utils.Utils
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

class ActivityAllowPermission : BaseActivity<ActivityAllowPermissionBinding>(), PermissionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnNext.setOnClickListener {
            val tedPermission = TedPermission.create()
            tedPermission.setDeniedMessage(getString(R.string.denied_permission))
            tedPermission.setPermissionListener(this)
            if (isTiramisuAbove()) {
                tedPermission.setPermissions(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                tedPermission.setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            tedPermission.check()
        }
    }


    @Suppress("KotlinConstantConditions")
    private fun runActivity(){
        PrefsUtils.saveFirst()
        if(Config.UI_VERSION == 1){
            Utils.startAct(this@ActivityAllowPermission, MainActivity::class.java)
            finish()
        } else {
            Utils.startAct(this@ActivityAllowPermission, MainActivity2::class.java)
            finish()
        }
    }

    private fun isTiramisuAbove() : Boolean{
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }


    override fun onPermissionGranted() {
        runActivity()
    }

    override fun onPermissionDenied(p0: MutableList<String>?) {
        finish()
    }
}