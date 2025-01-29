package com.pspgames.library.database

import android.content.Context
import com.ali77gh.easydata.sqlite.EasyTable
import com.pspgames.library.model.ModelCollection

class CollectionTable(context: Context) : EasyTable<ModelCollection>(context, ModelCollection::class.java, autoSetId = false){
    companion object {
        private var repo: CollectionTable? = null
        fun getRepo(context: Context): CollectionTable {
            if (repo ==null) repo = CollectionTable(context)
            return repo!!
        }
    }
}