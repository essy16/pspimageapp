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
import com.pspgames.library.databinding.DialogFilterCheckboxBinding
import com.pspgames.library.databinding.ItemGenreCheckBinding
import com.pspgames.library.model.ModelGenre

class DialogFilterGenre (context: Context, listGenre: ArrayList<ModelGenre>, callback: (String) -> Unit): Dialog(context, R.style
    .AlertDialog) {
    private val adapterFilter = AdapterFilterGenre()
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val binding : DialogFilterCheckboxBinding = DialogFilterCheckboxBinding .inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        binding.recyclerView.run {
            layoutManager = GridLayoutManager(context, 2)
            adapter = adapterFilter
            adapterFilter.setupData(listGenre)
        }
        binding.confirm.setOnClickListener {
            val stringBuilder = StringBuilder()
            adapterFilter.data.forEach {
                if(it.checked){
                    stringBuilder.append(it.genre)
                    stringBuilder.append(",")
                }
            }
            dismiss()
            callback.invoke(stringBuilder.toString().dropLast(1))
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