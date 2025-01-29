package com.pspgames.library.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.pspgames.library.dialog.DialogProgress
import com.pspgames.library.network.MainViewModel
import com.pspgames.library.network.RetrofitBuilder
import com.pspgames.library.network.ViewModelFactory

abstract class BaseFragment<V : ViewBinding> : Fragment() {
    private var _binding: V? = null
    val binding: V
        get() = _binding
            ?: throw RuntimeException("Should only use binding after onCreateView and before onDestroyView")
    private var _view: View? = null
    lateinit var viewModel: MainViewModel
    lateinit var loading: DialogProgress
    open fun onStarted(savedInstanceState: Bundle?) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupViewModel()
        loading = DialogProgress(requireContext())
        _binding = getBinding(inflater, container)
        onStarted(savedInstanceState)
        _view = binding.root

        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(RetrofitBuilder.build())
        ).get(MainViewModel::class.java)
    }
}
