package com.pspgames.library.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.pspgames.library.App
import com.pspgames.library.adapter.AdapterLatest
import com.pspgames.library.base.BaseFragment
import com.pspgames.library.base.handlerDelayed
import com.pspgames.library.databinding.FragmentFavourite2Binding

class FragmentFavourite2: BaseFragment<FragmentFavourite2Binding>() {
    private val adapterLatest = AdapterLatest()
    override fun onStarted(savedInstanceState: Bundle?) {
        super.onStarted(savedInstanceState)
        setupRecyclerView()
        setupSwipe()
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
        setupSwipe()
    }

    private fun setupSwipe(){
        binding.swipeRefresh.setOnRefreshListener {
            binding.layoutEmpty.visibility = View.GONE
            adapterLatest.clearData()
            handlerDelayed(1000){
                App.favouriteDatabase.toList().apply {
                    if(this.isNotEmpty()){
                        adapterLatest.setupData(this)
                        binding.layoutEmpty.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.VISIBLE
                    }
                }
                binding.swipeRefresh.isRefreshing = false
            }

        }
    }

    private fun setupRecyclerView(){
        binding.recyclerView.let {
            val layoutManager = GridLayoutManager(requireContext(), 2)
            it.layoutManager = layoutManager
            it.adapter = adapterLatest
            App.favouriteDatabase.toList().apply {
                if(this.isNotEmpty()){
                    adapterLatest.setupData(this)
                    binding.layoutEmpty.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.VISIBLE
                }
            }

        }
    }

}