package com.pspgames.library.base

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseMultipleAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listHolder: ArrayList<RecyclerView.ViewHolder> = arrayListOf()
    var data: ArrayList<T> = arrayListOf()
    var dataOriginal: ArrayList<T> = arrayListOf()

    protected abstract fun viewHolder(parent: ViewGroup): ArrayList<RecyclerView.ViewHolder>

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        @LayoutRes layout: Int
    ): RecyclerView.ViewHolder {
        return viewHolder(viewGroup)[layout]
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        convert(viewHolder, data[position], position, viewHolder.itemViewType)
    }

    abstract fun convert(viewHolder: RecyclerView.ViewHolder, item: T, position: Int, viewType: Int)

    override fun getItemCount(): Int {
        return data.size
    }

    abstract fun setViewType(position: Int): Int

    override fun getItemViewType(position: Int): Int {
        return setViewType(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun setupData(list: ArrayList<T>?) {
        if (list === this.data) {
            return
        }
        this.data.clear()
        this.data = list ?: arrayListOf()
        corMain {
            notifyDataSetChanged()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addMoreData(newData: ArrayList<T>) {
        this.data.addAll(newData)
        notifyItemRangeChanged(data.size - newData.size - 1, itemCount)
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