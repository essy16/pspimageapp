package com.pspgames.library.activity

import android.os.Bundle
import android.view.ViewGroup
import com.pspgames.library.R
import com.pspgames.library.base.BaseActivity
import com.pspgames.library.base.BaseRVAdapter
import com.pspgames.library.base.BaseViewHolder
import com.pspgames.library.base.toBinding
import com.pspgames.library.databinding.ActivityWelcomeBinding
import com.pspgames.library.databinding.ItemBoardBinding
import com.pspgames.library.utils.Utils

class ActivityWelcome : BaseActivity<ActivityWelcomeBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val adapterBoard = AdapterBoard()
        binding.viewPager.adapter = adapterBoard
        binding.viewPager.isUserInputEnabled = false
        val arrayTitle = resources.getStringArray(R.array.boarding_title)
        adapterBoard.setupData(arrayTitle.toCollection(ArrayList()))
        binding.viewPagerIndicator.setViewPager(binding.viewPager)
        adapterBoard.registerAdapterDataObserver(binding.viewPagerIndicator.adapterDataObserver)
        binding.btnNext.setOnClickListener {
            if(binding.btnNextText.text == getString(R.string.btn_next)){
                if(binding.viewPager.currentItem < (arrayTitle.size - 1)){
                    binding.viewPager.setCurrentItem(binding.viewPager.currentItem + 1, true)
                }
                if(binding.viewPager.currentItem == (arrayTitle.size - 1)){
                    binding.btnNextText.text = getString(R.string.btn_done)
                } else {
                    binding.btnNextText.text = getString(R.string.btn_next)
                }
            } else {
                Utils.startAct(this, ActivityAllowPermission::class.java)
                finish()
            }
        }
    }

    inner class AdapterBoard : BaseRVAdapter<String, ItemBoardBinding>(){
        override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemBoardBinding> {
            return BaseViewHolder(parent.toBinding())
        }

        override fun convert(binding: ItemBoardBinding, item: String, position: Int) {
            val arrayTitle = resources.getStringArray(R.array.boarding_title)
            val arrayDescription = resources.getStringArray(R.array.boarding_description)
            val arrayImage = resources.obtainTypedArray(R.array.boarding_image)
            binding.image.setImageResource(arrayImage.getResourceId(position, 0))
            binding.title.text = arrayTitle[position]
            binding.desc.text = arrayDescription[position]
            arrayImage.recycle()
        }
    }
}