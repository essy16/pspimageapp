package com.pspgames.library.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pspgames.library.R
import com.pspgames.library.adapter.AdapterLatest
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivityWallpaperListBinding
import com.pspgames.library.enums.Wallpaper
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.network.Status
import com.pspgames.library.ads.AdsUtils

@Suppress("DEPRECATION")
class ActivityWallpaperList2: BaseActivity<ActivityWallpaperListBinding>() {
    companion object {
        const val DATA = "DATA"
    }
    private val adapterLatest = AdapterLatest()
    private var currentPage = 1
    private var lastPage = 0
    private var isLoading = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsUtils.showBanner(this, binding.adsView)
        setupRecyclerView()
        binding.toolbarTitle.text = getTitle(intent.getStringExtra(DATA)!!)
        binding.toolbarBack.setOnClickListener {
            onBackPressed()
        }
        binding.swipeRefresh.setOnRefreshListener {
            adapterLatest.clearData()
            currentPage = 1
            lastPage = 0
            setupData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun getTitle(data: String) : String{
        when(data){
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setupRecyclerView(){
        binding.recyclerView.let {
            it.setHasFixedSize(true)
            val layoutManager = GridLayoutManager(this, 2)
            it.layoutManager = layoutManager
            it.adapter = adapterLatest
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val countItem = layoutManager.itemCount
                    val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                    val isLastPosition = countItem.minus(1) == lastVisiblePosition
                    if (isLastPosition && canLoad() && !isLoading) {
                        setupData(false)
                    }
                }
            })
            setupData()
        }
    }

    private fun setupData(first: Boolean? = true){
        isLoading = true
        getData {
            lastPage = it.last_page
            if(canLoad()){
                if(first!!){
                    adapterLatest.setupData(it.data)
                } else {
                    adapterLatest.addMoreData(it.data)
                }
                currentPage++
                isLoading = false
            }
        }

    }

    private fun getData(callback: (ModelLatest) -> Unit){
        val params: MutableMap<String, String> = HashMap()
        params["page"] = currentPage.toString()
        when(intent.getStringExtra(DATA)){
            Wallpaper.popular.name -> {
                viewModel.getPopular(params).observe(this) { status ->
                    when (status.status) {
                        Status.SUCCESS -> {
                            callback.invoke(status.data!!)
                            loading.dismiss()
                            setLoading(false)
                        }
                        Status.LOADING -> {
                            setLoading(true)
                        }
                        Status.ERROR -> {
                            setLoading(false)
                        }
                    }
                }
            }
            Wallpaper.latest.name -> {
                viewModel.getLatest(params).observe(this) { status ->
                    when (status.status) {
                        Status.SUCCESS -> {
                            callback.invoke(status.data!!)
                            loading.dismiss()
                            setLoading(false)
                        }
                        Status.LOADING -> {
                            setLoading(true)
                        }
                        Status.ERROR -> {
                            setLoading(false)
                        }
                    }
                }
            }
            Wallpaper.premium.name -> {
                params["id"] = "1"
                viewModel.getPremiumOrFree(params).observe(this) { status ->
                    when (status.status) {
                        Status.SUCCESS -> {
                            callback.invoke(status.data!!)
                            loading.dismiss()
                            setLoading(false)
                        }
                        Status.LOADING -> {
                            setLoading(true)
                        }
                        Status.ERROR -> {
                            setLoading(false)
                        }
                    }
                }
            }
            Wallpaper.free.name -> {
                params["id"] = "0"
                viewModel.getPremiumOrFree(params).observe(this) { status ->
                    when (status.status) {
                        Status.SUCCESS -> {
                            callback.invoke(status.data!!)
                            loading.dismiss()
                            setLoading(false)
                        }
                        Status.LOADING -> {
                            setLoading(true)
                        }
                        Status.ERROR -> {
                            setLoading(false)
                        }
                    }
                }
            }
            Wallpaper.live.name -> {
                viewModel.getLive(params).observe(this) { status ->
                    when (status.status) {
                        Status.SUCCESS -> {
                            callback.invoke(status.data!!)
                            loading.dismiss()
                            setLoading(false)
                        }
                        Status.LOADING -> {
                            setLoading(true)
                        }
                        Status.ERROR -> {
                            setLoading(false)
                        }
                    }
                }
            }
            Wallpaper.random.name -> {
                viewModel.getRandom(params).observe(this) { status ->
                    when (status.status) {
                        Status.SUCCESS -> {
                            callback.invoke(status.data!!)
                            loading.dismiss()
                            setLoading(false)
                        }
                        Status.LOADING -> {
                            setLoading(true)
                        }
                        Status.ERROR -> {
                            setLoading(false)
                        }
                    }
                }
            }
            Wallpaper.double.name -> {
                viewModel.getDouble(params).observe(this) { status ->
                    when (status.status) {
                        Status.SUCCESS -> {
                            callback.invoke(status.data!!)
                            loading.dismiss()
                            setLoading(false)
                        }
                        Status.LOADING -> {
                            setLoading(true)
                        }
                        Status.ERROR -> {
                            setLoading(false)
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(boolean: Boolean){
        if(boolean){
            loading.show()
        } else {
            loading.dismiss()
        }
    }

    private fun canLoad(): Boolean{
        return currentPage <= lastPage
    }
}