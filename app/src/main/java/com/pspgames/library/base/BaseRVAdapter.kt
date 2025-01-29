package com.pspgames.library.base

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

@Suppress(
    "unused",
    "MemberVisibilityCanBePrivate",
    "RedundantOverride",
    "RedundantVisibilityModifier"
)
abstract class BaseRVAdapter<T, V : ViewBinding> @JvmOverloads constructor(data: ArrayList<T>? = null) :
    RecyclerView.Adapter<BaseViewHolder<V>>() {
    var data: ArrayList<T> = data ?: arrayListOf()
    var arrayListSearch: ArrayList<T> = data ?: arrayListOf()
    lateinit var recyclerView: RecyclerView
    lateinit var binding: V
    lateinit var holder: BaseViewHolder<V>
    lateinit var context: Context
    protected abstract fun viewHolder(parent: ViewGroup): BaseViewHolder<V>
    abstract fun convert(binding: V, item: T, position: Int)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }
    open fun onCreate(context: Context){

    }
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        @LayoutRes layout: Int
    ): BaseViewHolder<V> {
        context = viewGroup.context
        onCreate(context)
        return viewHolder(viewGroup)
    }

    override fun onBindViewHolder(viewHolder: BaseViewHolder<V>, position: Int) {
        holder = viewHolder
        binding = viewHolder.binding
        convert(viewHolder.binding, data[position], position)
    }

    open fun setupData(list: MutableList<T>) {
        setupData(ArrayList(list))
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun setupData(list: ArrayList<T>) = apply {
        this.data.clear()
        this.data = list
        arrayListSearch.addAll(data)
        corMain {
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addMoreData(data: ArrayList<T>) {
        this.data.addAll(data)
        corMain {
            notifyItemRangeInserted(this.data.size, data.size)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun addData(list: ArrayList<T>, index: Int) {
        this.data.addAll(list)
        corMain {
            notifyDataSetChanged()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    open fun addData(item: T) {
        this.data.add(0, item)
        notifyItemInserted(0)
        handlerDelayed(1000){
            corMain {
                notifyDataSetChanged()
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    open fun addDatas(item: T) {
        this.data.add(0, item)
        notifyItemInserted(0)
        handlerDelayed(1000){
            corMain {
                notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun removeData(index: Int) {
        index.let {
            this.data.removeAt(it)
            notifyItemRemoved(it)
            handlerDelayed(1000){
                corMain {
                    notifyDataSetChanged()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun clearData() {
        data.clear()
        corMain {
            notifyDataSetChanged()
        }
    }
}