package ru.spbau.mit.pitersights.core

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import java.util.*

// TODO нам нужно откуда то извлекать позицию и описание, я пока не знаю откуда
// TODO geoPosition structure.
data class Sight(val id: String,
                 val name: String,
                 private val shortDescription: String,
                 private val longDescription: String,
                 val geoPosition: LatLng = LatLng(0.0, 0.0),
                 val link: String) : Parcelable {
    @Volatile var photo: String = ""
    var photoDate: Date? = null

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            LatLng(parcel.readDouble(), parcel.readDouble()),
            parcel.readString()) {
        photo = parcel.readString()
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
        parcel.writeString(photo)
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

    object COMPARATOR : Comparator<Sight> {
//        val random = Random()
        override fun compare(o1: Sight?, o2: Sight?): Int {
            if (o1 == null) {
                return if (o2 == null) 0 else -1
            }
            if (o2 == null) {
                return -1
            }

            // with photos weight more.
            if (o1.photo.isEmpty() != o2.photo.isEmpty()) {
                return if (o2.photo.isEmpty()) 1 else -1
            }

//             In alphabetical order reversed.
            return o2.name.compareTo(o1.name)
//            return random.nextInt(3) - 1
        }

    }
}

//data class Sights(private var sights: Map<Int, Sight> = emptyMap())