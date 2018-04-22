package ru.spbau.mit.pitersights.core

class Player(private val _geoPosition: String) {
    val geoPosition: String
        get() = _geoPosition // геолокация будет расчитываться как-то иначе


}