package com.pspgames.library.model


import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ModelConsole(
    @SerializedName("cid")
    var cid: String = "",
    @SerializedName("id")
    var id: String = "",
    @SerializedName("image")
    var image: String = "",
    @SerializedName("iso")
    var iso: String = "",
    @SerializedName("name")
    var name: String = "",
    @SerializedName("genre")
    var genre: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cid)
        parcel.writeString(id)
        parcel.writeString(image)
        parcel.writeString(iso)
        parcel.writeString(name)
        parcel.writeString(genre)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ModelConsole> {
        override fun createFromParcel(parcel: Parcel): ModelConsole {
            return ModelConsole(parcel)
        }

        override fun newArray(size: Int): Array<ModelConsole?> {
            return arrayOfNulls(size)
        }
    }
}