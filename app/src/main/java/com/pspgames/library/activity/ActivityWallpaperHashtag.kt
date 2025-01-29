package com.pspgames.library.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pspgames.library.adapter.AdapterLatest
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivityWallpaperListBinding
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.network.Status
import com.pspgames.library.ads.AdsUtils

@Suppress("DEPRECATION")
class ActivityWallpaperHashtag : BaseActivity<ActivityWallpaperListBinding>() {
    private val adapterLatest = AdapterLatest()
    private var currentPage = 1
    private var lastPage = 0
    private var isLoading = false
    private lateinit var item: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsUtils.showBanner(this, binding.adsView)
        item = intent.getStringExtra("hashtag")!!
        setupRecyclerView()
        binding.toolbarTitle.text = "#$item"
        binding.toolbarBack.setOnClickListener {
            onBackPressed()
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
            val layoutManager = GridLayoutManager(this, 3)
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
        params["query"] = item
        viewModel.getSearch(params).observe(this, { status ->
            when(status.status){
                Status.SUCCESS -> {
                    callback.invoke(status.data!!)
                    setLoading(false)
                }
                Status.LOADING -> {
                    setLoading(true)
                }
                Status.ERROR -> {
                    setLoading(false)
                }
            }
        })
    }

    private fun setLoading(boolean: Boolean){
       // binding.swipeRefresh.isRefreshing = boolean
    }


    private fun canLoad(): Boolean{
        return currentPage <= lastPage
    }
}