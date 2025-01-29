package com.pspgames.library.model


import android.os.Parcel
import android.os.Parcelable
import com.pspgames.library.model.ModelLatest.Data
import com.google.gson.annotations.SerializedName

data class ModelWallpaper(
    @SerializedName("cid")
    var cid: String = "",
    @SerializedName("color")
    var color: String = "",
    @SerializedName("download")
    var download: String = "",
    @SerializedName("form")
    var form: String = "",
    @SerializedName("genre")
    var genre: String = "",
    @SerializedName("id")
     var id: String = "",
    @SerializedName("image")
    var image: String = "",
    @SerializedName("image2")
    var image2: String = "",
    @SerializedName("image_gif")
    var imageGif: String = "",
    @SerializedName("iso")
    var iso: String = "",
    @SerializedName("premium")
    var premium: Int = 0,
    @SerializedName("tags")
    var tags: String = "",
    @SerializedName("title")
    var title: String = "",
    @SerializedName("type")
    var type: String = "",
    @SerializedName("view")
    var view: Int = 0,
    @SerializedName("zip_file1")
    var zipFile1: String = "",
    @SerializedName("zip_file2")
    var zipFile2: String = "",
    @SerializedName("zip_type")
    var zipType: String = ""
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cid)
        parcel.writeString(color)
        parcel.writeString(download)
        parcel.writeString(form)
        parcel.writeString(genre)
        parcel.writeString(id)
        parcel.writeString(image)
        parcel.writeString(image2)
        parcel.writeString(imageGif)
        parcel.writeString(iso)
        parcel.writeInt(premium)
        parcel.writeString(tags)
        parcel.writeString(title)
        parcel.writeString(type)
        parcel.writeInt(view)
        parcel.writeString(zipFile1)
        parcel.writeString(zipFile2)
        parcel.writeString(zipType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Data> {
        override fun createFromParcel(parcel: Parcel): Data {
            return Data(parcel)
        }

        override fun newArray(size: Int): Array<Data?> {
            return arrayOfNulls(size)
        }
    }
}