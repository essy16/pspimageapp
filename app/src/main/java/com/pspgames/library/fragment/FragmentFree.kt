package com.pspgames.library.fragment

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pspgames.library.adapter.AdapterLatest
import com.pspgames.library.base.BaseFragment
import com.pspgames.library.databinding.FragmentBaseBinding
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.network.Status

class FragmentFree: BaseFragment<FragmentBaseBinding>() {
    private val adapterLatest = AdapterLatest()
    private var currentPage = 1
    private var lastPage = 0
    private var isLoading = false
    override fun onStarted(savedInstanceState: Bundle?) {
        super.onStarted(savedInstanceState)
        setupRecyclerView()
        setupSwipe()
    }
    private fun setupSwipe(){
        binding.swipeRefresh.setOnRefreshListener {
            currentPage = 1
            lastPage = 1
            adapterLatest.clearData()
            setupData()
        }
    }

    private fun setupRecyclerView(){
        binding.recyclerView.let {
            val layoutManager = GridLayoutManager(requireContext(), 3)
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
        params["id"] = "0"
        viewModel.getPremiumOrFree(params).observe(this, { status ->
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
        binding.swipeRefresh.isRefreshing = boolean
    }

    private fun canLoad(): Boolean{
        return currentPage <= lastPage
    }

}