package com.pspgames.library.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pspgames.library.App
import com.pspgames.library.adapter.AdapterLatest
import com.pspgames.library.ads.AdsUtils
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.databinding.ActivitySearchBinding
import com.pspgames.library.model.ModelConsoleResult
import com.pspgames.library.model.ModelLatest
import com.pspgames.library.network.Status

class ActivityConsoleSearch : BaseActivity<ActivitySearchBinding>() {
    private val adapterConsole = AdapterLatest()
    private var currentPage = 1
    private var lastPage = 0
    private var isLoading = false
    private lateinit var search: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsUtils.showBanner(this, binding.adsView)
        setupButton()
        setupSearchView()
        setupRecyclerView()
        createInitialList()
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun createInitialList(){
        intent.getParcelableArrayListExtra<ModelLatest.Data>(DATA).run {
            this?.let {
                adapterConsole.setupData(it)
            }
        }
    }

    private fun setupButton() {
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSearchView() {
        binding.searchText.setOnEditorActionListener { textView, actionId, event ->
            if ((actionId == EditorInfo.IME_ACTION_SEARCH) || ((event.keyCode == KeyEvent.KEYCODE_ENTER) && (event.action == KeyEvent.ACTION_DOWN))) {
                currentPage = 1
                search = textView.text.toString()
                adapterConsole.clearData()
                setupData()
                hideKeyboard(textView)
                binding.searchText.clearFocus()
                true
            } else {
                false
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupRecyclerView() {
        binding.recyclerView.let {
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
        }
    }

    private fun setupData(first: Boolean? = true) {
        binding.swipeRefresh.isRefreshing = true
        isLoading = true
        getData {
            lastPage = it.lastPage
            if (canLoad()) {
                if (first!!) {
                    adapterConsole.setupData(it.consoles)
                } else {
                    adapterConsole.addMoreData(it.consoles)
                }
                currentPage++
                binding.swipeRefresh.isRefreshing = false
                isLoading = false
            }
        }
    }

    private fun getData(callback: (ModelConsoleResult) -> Unit) {
        viewModel.getConsoleByQuery(currentPage, search).observe(this) { status ->
            when (status.status) {
                Status.SUCCESS -> {
                    callback.invoke(status.data!!)
                    loading.dismiss()
                }

                Status.LOADING -> {
                    loading.show()
                }

                Status.ERROR -> {
                    loading.dismiss()
                    binding.swipeRefresh.isRefreshing = false
                    App.toast("not found")
                }
            }
        }
    }

    private fun canLoad(): Boolean {
        return currentPage <= lastPage
    }

    companion object {
        const val DATA = "DATA"
        fun start(context: Context, consoles: ArrayList<ModelLatest.Data>){
            Intent(context, ActivityConsoleSearch::class.java).run {
                putParcelableArrayListExtra(DATA, consoles)
                context.startActivity(this)
            }
        }
    }
}