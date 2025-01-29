package com.pspgames.library.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pspgames.library.adapter.AdapterLatest
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivityWallpaperListBinding
import com.pspgames.library.dialog.DialogFilter
import com.pspgames.library.enums.FilterMode
import com.pspgames.library.model.ModelConsoleCategory
import com.pspgames.library.model.ModelGenre
import com.pspgames.library.network.Status
import com.pspgames.library.utils.Utils

class ActivityConsoleList : BaseActivity<ActivityWallpaperListBinding>() {
    private lateinit var item: ModelConsoleCategory
    private val adapterConsole = AdapterLatest()
    private var currentPage = 1
    private var lastPage = 0
    private var isLoading = false
    private var genres = ""
    private var selectedFilter = FilterMode.DEFAULT
    private var listGenre: ArrayList<ModelGenre> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getSerializableExtra(DATA)?.let {
            item = it as ModelConsoleCategory
            binding.toolbarTitle.text = item.name
        }
        binding.toolbarBack.setOnClickListener {
            finish()
        }
        binding.search.visibility = View.VISIBLE
        binding.search.setOnClickListener {
            ActivityConsoleSearch.start(this, adapterConsole.data)
        }
        binding.filter.visibility = View.VISIBLE
        binding.filter.setOnClickListener {
            DialogFilter(this, selectedFilter, listGenre) { filterMode: FilterMode, s: String ->
                genres = s
                selectedFilter = filterMode
                binding.filterText.text = if(filterMode != FilterMode.DEFAULT){
                    if(genres.isNotEmpty()){
                        "Filters : ${filterMode.value} | $genres"
                    } else {
                        "Filters : ${filterMode.value}"
                    }
                } else {
                    if(genres.isNotEmpty()){
                        "Filters : $genres"
                    } else {
                        "Filters :"
                    }
                }

                setupData(true)
            }.show()
        }
        getGenre()
        setupRecyclerView()
    }

    private fun getGenre() {
        viewModel.getGenre().observe(this) { result ->
            result.data?.let { listGenre = it }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.let {
            it.setHasFixedSize(true)
            val layoutManager = GridLayoutManager(this, 2)
            it.layoutManager = layoutManager
            it.adapter = adapterConsole
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
            setupData(true)
        }
    }

    private fun setupData(first: Boolean) {
        isLoading = true
        if (first) {
            currentPage = 1
            adapterConsole.clearData()
        }
        val getter = when (selectedFilter) {
            FilterMode.POPULAR -> viewModel.getConsolePopular(currentPage, item.cid, genres)
            FilterMode.UPDATED -> viewModel.getConsoleUpdated(currentPage, item.cid, genres)
            FilterMode.LATEST -> viewModel.getConsoleLatest(currentPage, item.cid, genres)
            FilterMode.TOP_RATED -> viewModel.getConsoleRated(currentPage, item.cid, genres)
            FilterMode.DEFAULT -> viewModel.getConsoleLatest(currentPage, item.cid, genres)
        }
        getter.observe(this) { status ->
            when (status.status) {
                Status.SUCCESS -> {
                    status.data?.let {
                        lastPage = it.lastPage
                        if (canLoad()) {
                            if (first) {
                                adapterConsole.setupData(it.consoles)
                            } else {
                                adapterConsole.addMoreData(it.consoles)
                            }
                            currentPage++
                            isLoading = false
                        }
                    }
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

    private fun canLoad(): Boolean {
        return currentPage <= lastPage
    }

    companion object {
        const val DATA = "DATA"
        fun start(context: Context, item: ModelConsoleCategory) {
            val bundle = Bundle()
            bundle.putSerializable(DATA, item)
            Utils.startAct(context, ActivityConsoleList::class.java, bundle)
        }
    }
}