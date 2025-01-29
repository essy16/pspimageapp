package com.pspgames.library.database

import android.content.Context
import com.ali77gh.easydata.sqlite.EasyTable
import com.pspgames.library.model.ModelLatest

class FavouriteTable(context: Context) : EasyTable<ModelLatest.Data>(context, ModelLatest.Data::class.java, autoSetId = false){
    companion object {
        private var repo: FavouriteTable? = null
        fun getRepo(context: Context): FavouriteTable {
            if (repo ==null) repo = FavouriteTable(context)
            return repo!!
        }
    }
}