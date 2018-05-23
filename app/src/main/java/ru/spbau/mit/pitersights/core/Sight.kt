package ru.spbau.mit.pitersights.core

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

// TODO нам нужно откуда то извлекать позицию и описание, я пока не знаю откуда
// TODO geoPosition structure.
data class Sight(val id: String,
                 val name: String,
                 private val shortDescription: String,
                 private val longDescription: String,
                 val geoPosition: LatLng = LatLng(0.0, 0.0),
                 val link: String) : Parcelable {
    var _photo: String = ""
    var photo: String
        get() = _photo
        set(value) {
            _photo = value
        }

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            LatLng(parcel.readDouble(), parcel.readDouble()),
            parcel.readString()) {
        _photo = parcel.readString()
    }

    fun getFullDescription() = longDescription

    fun getShortDescription(): String {
        var splitResult = shortDescription.split("\n")
        while (splitResult.size > 1 && splitResult.last() == "") {
            splitResult = splitResult.dropLast(1)
        }
        return splitResult[0]
    }
    
    fun isAddedToStorage() = !photo.isEmpty()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(shortDescription)
        parcel.writeString(longDescription)
        parcel.writeDouble(geoPosition.latitude)
        parcel.writeDouble(geoPosition.longitude)
        parcel.writeString(_photo)
    }

    override fun describeContents(): Int {
        return 0
    }

    object CREATOR : Parcelable.Creator<Sight> {
        override fun createFromParcel(parcel: Parcel): Sight {
            return Sight(parcel)
        }

        override fun newArray(size: Int): Array<Sight?> {
            return arrayOfNulls(size)
        }
    }
}

//data class Sights(private var sights: Map<Int, Sight> = emptyMap())