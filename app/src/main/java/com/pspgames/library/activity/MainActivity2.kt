package com.pspgames.library.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.pspgames.library.App
import com.pspgames.library.R
import com.pspgames.library.ads.AdmobUtils
import com.pspgames.library.ads.AdsUtils
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivityMain2Binding
import com.pspgames.library.fragment.FragmentCategories2
import com.pspgames.library.fragment.FragmentDashboard2
import com.pspgames.library.fragment.FragmentDual
import com.pspgames.library.fragment.FragmentFavourite2
import com.pspgames.library.utils.PrefsUtils
import com.benkkstudios.meombottomnav.MeowBottomNavigation
import com.michaelflisar.gdprdialog.GDPR
import com.michaelflisar.gdprdialog.GDPRConsentState
import com.michaelflisar.gdprdialog.GDPRDefinitions
import com.michaelflisar.gdprdialog.GDPRSetup
import com.michaelflisar.gdprdialog.helper.GDPRPreperationData

class MainActivity2 : BaseActivity<ActivityMain2Binding>(), GDPR.IGDPRCallback {
    private val mSetup = GDPRSetup(GDPRDefinitions.ADMOB, GDPRDefinitions.APPLOVIN, GDPRDefinitions.IRONSOURCE, GDPRDefinitions.STARTAPP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsUtils.init(this)
        showGDPRIfNecessary()
    }

    private fun setupBottomNav() {
        binding.navigation.add(MeowBottomNavigation.Model(0, R.drawable.ic_nav_home))
        binding.navigation.add(MeowBottomNavigation.Model(1, R.drawable.ic_nav_categories))
        binding.navigation.add(MeowBottomNavigation.Model(2, R.drawable.ic_nav_favourite))
        binding.navigation.show(0, false)

        val adapter = ViewPagerFragmentAdapter(supportFragmentManager, lifecycle)
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = adapter.itemCount
        binding.viewPager.adapter = adapter
        binding.viewPager.setPageTransformer(MarginPageTransformer(1500))
        binding.navigation.setOnClickMenuListener {
            binding.viewPager.currentItem = it.id
        }
    }

    private var doubleBackToExitPressedOnce = false

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            if (doubleBackToExitPressedOnce) {
                moveTaskToBack(true)
                return
            }

            this.doubleBackToExitPressedOnce = true
            App.toast(getString(R.string.back_exit))
            Handler(Looper.myLooper()!!).postDelayed(
                {
                    doubleBackToExitPressedOnce = false

                }, 1000
            )
        } else {
            binding.navigation.show(0, false)
            binding.viewPager.currentItem = 0
        }
    }

    class ViewPagerFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return FragmentDashboard2()
                1 -> return FragmentCategories2()
                2 -> return FragmentFavourite2()
                3 -> return FragmentDual()
            }
            return FragmentDashboard2()
        }

        override fun getItemCount(): Int {
            return 4
        }
    }

    private fun showGDPRIfNecessary() {
        GDPR.getInstance().checkIfNeedsToBeShown(this, mSetup)
    }

    override fun onConsentNeedsToBeRequested(data: GDPRPreperationData?) {
        GDPR.getInstance().showDialog(this, mSetup, data!!.location)
    }

    override fun onConsentInfoUpdate(consentState: GDPRConsentState?, isNewState: Boolean) {
        if (PrefsUtils.getAdmob().nativeEnable == 1) {
            loading.show()
            AdmobUtils.loadNativeAds(this) {
                loading.dismiss()
            }
        }
        setupBottomNav()
    }
}