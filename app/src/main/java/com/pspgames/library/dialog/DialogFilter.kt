package com.pspgames.library.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.GridLayoutManager
import com.pspgames.library.R
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.DialogFilterBinding
import com.pspgames.library.databinding.ItemGenreCheckBinding
import com.pspgames.library.enums.FilterMode
import com.pspgames.library.model.ModelGenre

class DialogFilter(context: Context, selectedFilter: FilterMode, listGenre: ArrayList<ModelGenre>, callback: (FilterMode, String) -> Unit):
    Dialog(context, R.style.AlertDialog) {
    private var filterMode = FilterMode.DEFAULT
    private val adapterFilter = DialogFilterGenre.AdapterFilterGenre()
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val binding : DialogFilterBinding = DialogFilterBinding .inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        binding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 2)
            adapter = adapterFilter
            adapterFilter.setupData(listGenre)
        }
        when(selectedFilter){
            FilterMode.POPULAR -> binding.popular.isChecked = true
            FilterMode.UPDATED -> binding.updated.isChecked = true
            FilterMode.LATEST -> binding.latest.isChecked = true
            FilterMode.TOP_RATED -> binding.rated.isChecked = true
            FilterMode.DEFAULT -> { binding.defaults.isChecked = true}
        }
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId  ->
            when(checkedId){
                R.id.popular -> filterMode = FilterMode.POPULAR
                R.id.updated -> filterMode = FilterMode.UPDATED
                R.id.latest -> filterMode = FilterMode.LATEST
                R.id.rated -> filterMode = FilterMode.TOP_RATED
                R.id.defaults -> filterMode = FilterMode.DEFAULT
            }
        }
        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.confirm.setOnClickListener {
            dismiss()
            val stringBuilder = StringBuilder()
            adapterFilter.data.forEach {
                if(it.checked){
                    stringBuilder.append(it.genre)
                    stringBuilder.append(",")
                }
            }
            val genres = stringBuilder.toString().dropLast(1)
            callback.invoke(filterMode, genres)
        }
    }

    class AdapterFilterGenre : BaseRVAdapter<ModelGenre, ItemGenreCheckBinding>(){
        override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemGenreCheckBinding> {
            return BaseViewHolder(parent.toBinding())
        }

        override fun convert(binding: ItemGenreCheckBinding, item: ModelGenre, position: Int) {
            binding.checkbox.isChecked = item.checked
            binding.checkbox.text = item.genre
            binding.checkbox.setOnCheckedChangeListener { _, checked ->
                item.checked = checked
            }
        }
    }
}