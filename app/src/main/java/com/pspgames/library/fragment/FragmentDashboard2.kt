package com.pspgames.library.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import coil.load
import coil.request.videoFrameMillis
import com.pspgames.library.Config
import com.pspgames.library.R
import com.pspgames.library.activity.ActivityDetail
import com.pspgames.library.activity.ActivityDownload
import com.pspgames.library.activity.ActivitySearch
import com.pspgames.library.activity.ActivitySettings
import com.pspgames.library.activity.ActivityWallpaperList2
import com.pspgames.library.adapter.AdapterBanner
import com.pspgames.library.adapter.AdapterColor
import com.pspgames.library.adapter.AdapterConsoleCategory
import com.pspgames.library.base.BaseFragment
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.FragmentDashboard2Binding
import com.pspgames.library.databinding.ItemDashboardBinding
import com.pspgames.library.databinding.ItemWallpaperHorizontalBinding
import com.pspgames.library.enums.Wallpaper
import com.pspgames.library.model.ModelHome
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.network.Status
import com.pspgames.library.utils.Utils
import com.pspgames.library.utils.isConsole

class FragmentDashboard2: BaseFragment<FragmentDashboard2Binding>() {
    private val adapterDashboard2 = AdapterDashboard2()
    private lateinit var adapterFeatured : AdapterBanner
    override fun onStarted(savedInstanceState: Bundle?) {
        super.onStarted(savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapterDashboard2
        getConsoleCategory()
        getColor()
        getHome {
            adapterDashboard2.setupData(it)
        }
        binding.buttonSettings.setOnClickListener {
            Utils.startAct(requireContext(), ActivitySettings::class.java)
        }
        binding.buttonSearch.setOnClickListener {
            ActivitySearch.start(requireContext(), adapterFeatured.data)
        }
        binding.buttonDownload.setOnClickListener {
            ActivityDownload.start(requireContext())
        }
        binding.swipeRefresh.setOnRefreshListener {
            adapterFeatured.clearData()
            adapterDashboard2.clearData()
            getHome {
                adapterDashboard2.setupData(it)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }


    private fun getConsoleCategory() {
        viewModel.getConsoleCategory().observe(this) { status ->
            when (status.status) {
                Status.SUCCESS -> {
                    status.data?.let {
                        val adapterColor = AdapterConsoleCategory()
                        binding.recyclerViewConsole.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        binding.recyclerViewConsole.adapter = adapterColor
                        adapterColor.setupData(it)
                    }
                }
                Status.LOADING -> {
                    loading.show()
                }
                Status.ERROR -> {
                    loading.dismiss()
                }
            }
        }
    }

    private fun getColor() {
        viewModel.getColor().observe(this) { status ->
            when (status.status) {
                Status.SUCCESS -> {
                    val adapterColor = AdapterColor()
                    binding.recyclerViewColor.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.recyclerViewColor.adapter = adapterColor
                    adapterColor.setupData(status.data!!)
                }
                Status.LOADING -> {
                    loading.show()
                }
                Status.ERROR -> {
                    loading.dismiss()
                }
            }
        }
    }

    private fun getHome(callback: (ArrayList<ModelHome>) -> Unit) {
        viewModel.getHome().observe(this) { status ->
            when (status.status) {
                Status.SUCCESS -> {
                    for (i in 0 until status.data!!.size) {
                        if (status.data[i].name == Config.HOME_BANNER.name) {
                            setupBanner(status.data[i].data, getTitle(status.data[i]))
                            status.data.removeAt(i)
                            break
                        }
                    }
                    callback.invoke(status.data)
                    loading.dismiss()
                }
                Status.LOADING -> {
                    loading.show()
                }
                Status.ERROR -> {
                    loading.dismiss()
                }
            }
        }
    }

    private fun setupBanner(banner: ArrayList<ModelLatest.Data>, title: String){
        if(Config.ENABLE_HOME_BANNER){
            adapterFeatured = AdapterBanner(title)
            binding.viewPager.adapter = adapterFeatured
            binding.viewPager.clipToPadding = false
            binding.viewPager.clipChildren = false
            binding.viewPager.offscreenPageLimit = 3
            binding.viewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            val compositePageTransformer =  CompositePageTransformer()
            compositePageTransformer.addTransformer( MarginPageTransformer(5))
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - kotlin.math.abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }
            binding.viewPager.setPageTransformer(compositePageTransformer)
            binding.viewPagerIndicator.setViewPager(binding.viewPager)
            adapterFeatured.registerAdapterDataObserver(binding.viewPagerIndicator.adapterDataObserver)
            adapterFeatured.clearData()
            adapterFeatured.setupData(banner)
            if(adapterFeatured.data.size >= 2) binding.viewPager.setCurrentItem(1, false)
        } else {
            binding.viewPager.visibility = View.GONE
            binding.viewPagerIndicator.visibility = View.GONE
        }
    }

    private fun getTitle(item: ModelHome) : String{
        when(item.name){
            Wallpaper.latest.name -> {
                return getString(R.string.tab_latest2)
            }
            Wallpaper.popular.name -> {
                return getString(R.string.tab_popular2)
            }
            Wallpaper.premium.name -> {
                return getString(R.string.tab_premium2)
            }
            Wallpaper.free.name -> {
                return getString(R.string.tab_free2)
            }
            Wallpaper.live.name -> {
                return getString(R.string.tab_live2)
            }
            Wallpaper.random.name -> {
                return getString(R.string.tab_random2)
            }
            Wallpaper.double.name -> {
                return getString(R.string.tab_double)
            }
            else -> {
                return ""
            }
        }
    }

    inner class AdapterDashboard2: BaseRVAdapter<ModelHome, ItemDashboardBinding>(){
        override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemDashboardBinding> {
            return BaseViewHolder(parent.toBinding())
        }

        override fun convert(binding: ItemDashboardBinding, item: ModelHome, position: Int) {
            if(item.name != Config.HOME_BANNER.name){
                binding.title.text = getTitle(item)
                val adapterWallpaperHorizontal = AdapterWallpaperHorizontal(item.data)
                binding.recyclerViewChild.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.recyclerViewChild.adapter = adapterWallpaperHorizontal
                adapterWallpaperHorizontal.setupData(item.data)
                binding.viewAll.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString(ActivityWallpaperList2.DATA, item.name)
                    Utils.startAct(requireContext(), ActivityWallpaperList2::class.java, bundle)
                }
            }
        }

        inner class AdapterWallpaperHorizontal(val arrayList: ArrayList<ModelLatest.Data>): BaseRVAdapter<ModelLatest.Data, ItemWallpaperHorizontalBinding>() {
            override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemWallpaperHorizontalBinding> {
                return BaseViewHolder(parent.toBinding())
            }

            @SuppressLint("CheckResult")
            override fun convert(binding: ItemWallpaperHorizontalBinding, item: ModelLatest.Data, position: Int) {
                val context = binding.root.context
                val thumbnail = when (item.type) {
                    "DOUBLE" -> {
                        item.imageGif
                    }
                    else -> {
                        Utils.generateThumbnail(item.image, 200)
                    }
                }
                binding.wallpaperImage.load(thumbnail) {
                    crossfade(false)
                    placeholder(R.drawable.placeholder)
                    videoFrameMillis(1)
                    listener(
                        onSuccess = { _, _ ->
                            binding.progressBar.visibility = View.GONE
                        }
                    )
                }
                binding.title.text = item.title
                binding.wallpaperView.text = item.view.toString()
                binding.wallpaperPremium.visibility = if(item.premium == 0) View.GONE else View.VISIBLE
                binding.cardType.visibility = if(item.type == "IMAGE") View.GONE else View.VISIBLE
                if(item.isConsole()){
                    binding.cardType.visibility = View.GONE
                }
                when (item.type) {
                    "VIDEO" -> {
                        binding.wallpaperType.text = context.getString(R.string.type_video)
                    }
                    "GIF" -> {
                        binding.wallpaperType.text = context.getString(R.string.type_gif)
                    }
                    else -> {
                        binding.wallpaperType.text = context.getString(R.string.type_double)
                    }
                }
                binding.root.setOnClickListener {
                    arrayList[position].view += 1
                    notifyItemChanged(position)
                    val trimList =  ArrayList<ModelLatest.Data>(arrayList.subList(position, arrayList.size))
                    val bundle = Bundle()
                    bundle.putParcelableArrayList("item", trimList)
                    bundle.putInt("position", position)
                    Utils.startAct(requireContext(), ActivityDetail::class.java, bundle)
                }
            }
        }
    }
}