package com.pspgames.library.activity

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import com.pspgames.library.App
import com.pspgames.library.adapter.AdapterDownload
import com.pspgames.library.ads.AdsUtils
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.database.DownloadTable
import com.pspgames.library.databinding.ActivityDownloadBinding
import com.pspgames.library.downloader.DownloadManager
import com.pspgames.library.downloader.DownloadWorker
import com.pspgames.library.interfaces.DownloadListener
import com.pspgames.library.model.ModelDownload
import com.pspgames.library.utils.Utils

class ActivityDownload : BaseActivity<ActivityDownloadBinding>(), DownloadListener {
    private val downloadTable by lazy { DownloadTable.getRepo(this) }
    private val downloadManager by lazy { DownloadManager(this) }
    private val workersObserver = Observer { workers: List<WorkInfo?>? ->
        if (!workers.isNullOrEmpty()) {
            val worker = workers.first()
            onWorkInfoChanged(worker)
        }
    }
    private val adapterDownload by lazy { AdapterDownload(downloadTable) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.toolbarBack.setOnClickListener {
            finish()
        }
        setupRecyclerView()
        AdsUtils.showBanner(this, binding.adsLayout)
        downloadManager.setListener(this)
        downloadManager.setObserver(workersObserver)
        downloadManager.download()
    }

    fun retry() {
        downloadManager.download()
    }

    fun cancel() {
        downloadManager.cancel()
    }

    fun pause() {
        downloadManager.onPause()
    }

    fun resume() {
        downloadManager.onResume()

    }

    private fun onWorkInfoChanged(workInfo: WorkInfo?) {
        workInfo?.let {
            when (it.state) {
                WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
                    -> {
                    val downloadId = it.progress.getString(DownloadWorker.DOWNLOAD_ID)
                    if (downloadId != null) {
                        val downloadModel = downloadTable.getById(downloadId)
                        adapterDownload.update(downloadModel)
                    }
                }

                WorkInfo.State.CANCELLED -> {
                    App.log("CANCELLED")
                }

                else -> {}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        downloadManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        downloadManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadManager.onDestroy()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.let {
            it.itemAnimator = null
            val layoutManager = LinearLayoutManager(this)
            it.layoutManager = layoutManager
            it.adapter = adapterDownload
            adapterDownload.setupData(downloadTable.toList().asReversed())
        }
    }

    companion object {
        fun start(context: Context) {
            Utils.startAct(context, ActivityDownload::class.java)
        }
    }

    override fun onCancel(modelDownload: ModelDownload) {
        adapterDownload.update(modelDownload)
        downloadTable.update(modelDownload)
    }

    override fun onPause(modelDownload: ModelDownload) {
        adapterDownload.update(modelDownload)
        downloadTable.update(modelDownload)
    }

    override fun onResume(modelDownload: ModelDownload) {
        adapterDownload.update(modelDownload)
        downloadTable.update(modelDownload)
    }

    override fun onComplete(modelDownload: ModelDownload) {
        adapterDownload.update(modelDownload)
        downloadTable.update(modelDownload)
    }
}