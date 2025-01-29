package com.pspgames.library.fragment

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.pspgames.library.base.*
import com.pspgames.library.databinding.FragmentDualBinding
import com.pspgames.library.databinding.ItemWallpaperBinding
import com.pspgames.library.model.ModelDual


class FragmentDual: BaseFragment<FragmentDualBinding>() {
    override fun onStarted(savedInstanceState: Bundle?) {
        super.onStarted(savedInstanceState)
        val adapter = AdapterDual()
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter
        val list: ArrayList<ModelDual> = arrayListOf()
        list.add(ModelDual(2, 0, "0", "https://i.postimg.cc/ncCBbJB9/lock.jpg", "https://i.postimg.cc/TYzyzWTB/home.jpg", 0, "", "", 1))
        list.add(ModelDual(2, 0, "0", "https://i.postimg.cc/76rf55Bj/home.jpg", "https://i.postimg.cc/DwbGm3vJ/lock.jpg", 0, "", "", 1))
        list.add(ModelDual(2, 0, "0", "https://i.postimg.cc/ncCBbJB9/lock.jpg", "https://i.postimg.cc/TYzyzWTB/home.jpg", 0, "", "", 1))
        list.add(ModelDual(2, 0, "0", "https://i.postimg.cc/ncCBbJB9/lock.jpg", "https://i.postimg.cc/TYzyzWTB/home.jpg", 0, "", "", 1))
        list.add(ModelDual(2, 0, "0", "https://i.postimg.cc/ncCBbJB9/lock.jpg", "https://i.postimg.cc/TYzyzWTB/home.jpg", 0, "", "", 1))
        adapter.setupData(list)
    }

    inner class AdapterDual: BaseRVAdapter<ModelDual, ItemWallpaperBinding>(){
        override fun viewHolder(parent: ViewGroup): BaseViewHolder<ItemWallpaperBinding> {
            return BaseViewHolder(parent.toBinding())
        }

        override fun convert(binding: ItemWallpaperBinding, item: ModelDual, position: Int) {
            val context = binding.root.context
            val animation = AnimationDrawable()
            binding.progressBar.visibility = View.GONE
            imageToDrawable(item.image){ darawable1 ->
                animation.addFrame(darawable1, 500)
                imageToDrawable(item.image_second){ drawable2 ->
                    animation.addFrame(drawable2, 500)
                    animation.isOneShot = false
                    binding.wallpaperImage.setImageDrawable(animation)
                    animation.start()
                }
            }


            val wallpaperManager = WallpaperManager.getInstance(context)
            binding.wallpaperView.text = item.view.toString()
            binding.wallpaperPremium.visibility = if(item.premium == 0) View.GONE else View.VISIBLE
            binding.cardType.visibility = if(item.type == "IMAGE") View.GONE else View.VISIBLE
            binding.root.setOnClickListener {
                imageToBitmap(item.image){
                    wallpaperManager.setBitmap(it)
                }
                imageToBitmap(item.image_second){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(it, null,
                            true,
                            WallpaperManager.FLAG_LOCK)
                    }
                }
            }
        }

    }

    private fun imageToDrawable(image: String, callback: (Drawable) -> Unit){

    }

    private fun imageToBitmap(image: String, callback: (Bitmap) -> Unit){

    }
}