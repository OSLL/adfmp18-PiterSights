package ru.spbau.mit.pitersights.core

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil.computeHeading
import kotlin.math.abs

class Geographer {

    var sights : List<Sight> = emptyList()

    private val aperture = 10.0f
    // надо еще компас всунуть сюда как-то

    fun calculateDistance(player: Player): Map<Sight, Float> {
        val playerPosition = player.geoLocation
        val distances = mutableMapOf<Sight, Float>()
        for (sight in sights) {
            val dists = FloatArray(1)
            if (playerPosition != null) {
                Location.distanceBetween(playerPosition.latitude, playerPosition.longitude,
                        sight.geoPosition.latitude, sight.geoPosition.longitude, dists)
            }
            distances[sight] = dists[0]
        }

        return distances
                .toList()
                .sortedBy { (_, value) -> value }
                .slice(0 until 10)
                .toMap()
    }

    fun detectSight(player: Player, neighbors: Map<Sight, Float>): Sight? {
        val playerLocation = player.geoLocation
        val playerViewDirection = playerLocation!!.bearing
        val sights = neighbors.keys
        val sightsInView = mutableSetOf<Sight>()
        for (sight in sights) {
            val heading = computeHeading(
                    LatLng(playerLocation.latitude, playerLocation.longitude),
                    sight.geoPosition
            )
            if (abs(90 - heading - playerViewDirection) < aperture / 2) {
                sightsInView.add(sight)
            }
        }

        var closestSightDistance: Float? = Float.MAX_VALUE
        var closestSight: Sight? = null
        for (sight in sightsInView) {
            if (neighbors[sight]!! < closestSightDistance!!) {
                closestSight = sight
                closestSightDistance = neighbors[sight]
            }
        }

        return if (closestSightDistance != null &&  closestSightDistance > 100.0f) {
            null
        } else {
            closestSight
        }

    }

    fun getLeftNearSights(player: Player, neighbors: Map<Sight, Float>): Map<Sight, Float> {
        val playerLocation = player.geoLocation
        val playerViewDirection = playerLocation!!.bearing
        val leftNeighbors = mutableMapOf<Sight, Float>()
        for (sight in neighbors) {
            val heading = computeHeading(
                    LatLng(playerLocation.latitude, playerLocation.longitude),
                    sight.key.geoPosition
            )
            if (playerViewDirection - heading < 0) {
                leftNeighbors[sight.key] = sight.value
            }
        }
        return leftNeighbors
    }

    fun getRightNearSights(player: Player, neighbors: Map<Sight, Float>): Map<Sight, Float> {
        val playerLocation = player.geoLocation
        val playerViewDirection = playerLocation!!.bearing
        val rightNeighbors = mutableMapOf<Sight, Float>()
        for (sight in neighbors) {
            val heading = computeHeading(
                    LatLng(playerLocation.latitude, playerLocation.longitude),
                    sight.key.geoPosition
            )
            if (heading - playerViewDirection < 0) {
                rightNeighbors[sight.key] = sight.value
            }
        }
        return rightNeighbors
    }
}