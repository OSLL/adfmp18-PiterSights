package ru.spbau.mit.pitersights.core

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

// TODO нам нужно откуда то извлекать позицию и описание, я пока не знаю откуда
// TODO geoPosition structure.
data class Sight(val name: String,
                 val description: List<String> = emptyList(), // три элемента
                 val imageResource: Int,
                 val geoPosition: LatLng = LatLng(0.0, 0.0),
                 val link: String) : Parcelable {
    val id = IdSetter.create(); get

    var _photo: String = ""
    var photo: String
        get() = _photo
        set(value) {
            _photo = value
        }

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.createStringArrayList(),
            parcel.readInt(),
            LatLng(parcel.readDouble(), parcel.readDouble())) {
        _photo = parcel.readString()
    }

    fun getFullDescription() = description.get(0)
    fun getCameraDescription() = description.get(1)
    fun getMapDescription() = description.get(2)
    fun isAddedToStorage() = !photo.isEmpty()

    companion object IdSetter {
        private var id = 0
        fun create() {
            id += 1
            id
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeStringList(description)
        parcel.writeInt(imageResource)
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