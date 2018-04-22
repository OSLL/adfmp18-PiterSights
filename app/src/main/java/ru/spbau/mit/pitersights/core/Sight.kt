package ru.spbau.mit.pitersights.core

class Sight(private val _name: String, private val sigthsResource: String) { // нам нужно откуда то извлекать позицию и описание, я пока не знаю откуда
    val id = IdSetter.create(); get
    val geoPosition = ""; get // это не строка, но пока пусть будет строка

    val name: String
        get() = _name

    private val description = emptyList<String>() // три элемента
    var _photo: String = ""
    var photo: String
        get() = _photo
        set(value) {
            _photo = value
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
}

data class Sights(private var sights: Map<Int, Sight> = emptyMap())