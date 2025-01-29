package com.pspgames.library.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.pspgames.library.App
import com.pspgames.library.R
import com.pspgames.library.adapter.AdapterLatest
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.base.handlerDelayed
import com.pspgames.library.databinding.ActivityCollectionBinding
import com.pspgames.library.utils.Converter


class ActivityCollection : BaseActivity<ActivityCollectionBinding>() {
    private val adapterLatest = AdapterLatest()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.toolbar.title = getString(R.string.tab_collection)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
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
                App.collectionTable.toList().apply {
                    if(this.isNotEmpty()){
                        adapterLatest.setupData(Converter.collectionToFavouriteList(this))
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
            val layoutManager = GridLayoutManager(this, 2)
            it.layoutManager = layoutManager
            it.adapter = adapterLatest
            App.collectionTable.toList().apply {
                if(this.isNotEmpty()){
                    adapterLatest.setupData(Converter.collectionToFavouriteList(this))
                    binding.layoutEmpty.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.VISIBLE
                }
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}