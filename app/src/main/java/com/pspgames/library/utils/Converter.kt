package com.pspgames.library.utils

import com.pspgames.library.model.ModelCollection
import com.pspgames.library.model.ModelLatest

object Converter {
    fun favouriteToCollection(data: ModelLatest.Data, localImage: String) : ModelCollection{
        return ModelCollection(
            cid = data.cid,
            color = data.color,
            download = data.download,
            form = data.form,
            genre = data.genre,
            id = data.id,
            image = data.image,
            image2 = data.image2,
            image_gif = data.imageGif,
            iso = data.iso,
            premium = data.premium,
            tags = data.tags,
            title = data.title,
            type = data.type,
            view = data.view,
            zipFile1 = data.zipFile1,
            zipFile2 = data.zipFile2,
            zipType = data.zipType,
            localImage = localImage
        )
    }

    fun collectionToFavourite(data: ModelCollection) :  ModelLatest.Data{
        return  ModelLatest.Data(
            cid = data.cid,
            color = data.color,
            download = data.download,
            form = data.form,
            genre = data.genre,
            id = data.id,
            image = data.image,
            image2 = data.image2,
            imageGif = data.image_gif,
            iso = data.iso,
            premium = data.premium,
            tags = data.tags,
            title = data.title,
            type = data.type,
            view = data.view,
            zipFile1 = data.zipFile1,
            zipFile2 = data.zipFile2,
            zipType = data.zipType,
        )
    }
    fun collectionToFavouriteList(list: ArrayList<ModelCollection>) :  ArrayList<ModelLatest.Data>{
        val resultList = arrayListOf<ModelLatest.Data>()
        list.forEach {
            resultList.add(collectionToFavourite(it))
        }
        return resultList
    }
}