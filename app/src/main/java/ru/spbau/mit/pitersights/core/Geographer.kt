package ru.spbau.mit.pitersights.core

class Geographer {
    // надо еще компас всунуть сюда как-то

    fun calculateDistance(player: Player): Map<Sight, String> {
        //используем data class sights и геолокацию игрока вычисляет близайшие объекты и расстояние до них
        return emptyMap()
    }

    fun detectSight(player: Player, neighbors: Map<Sight, String>): Sight? {
        // определяет объект, на который мы смотрим, либо его отсутсвие
        return null
    }
}