package ru.spbau.mit.pitersights.core

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil.computeHeading
import kotlin.math.abs
import kotlin.math.min

class Geographer {

    var sights : List<Sight> = emptyList()

    fun calculateDistance(player: Player): MutableMap<Sight, Float> {
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
                .slice(0 until 11)
                .toMap()
                .toMutableMap()
    }

    fun detectSight(player: Player, neighbors: Map<Sight, Float>): Sight? {
        val playerLocation = player.geoLocation
        var playerViewDirection = playerLocation!!.bearing
        if (playerViewDirection > 180) {
            playerViewDirection -= 360
        }
        val sights = neighbors.keys
        val sightsInView = mutableSetOf<Sight>()
        for (sight in neighbors) {
            var heading = computeHeading(
                    LatLng(playerLocation.latitude, playerLocation.longitude),
                    sight.key.geoPosition
            )
            val aperture = getAperture(sight.value)
            if (abs(heading - playerViewDirection) < aperture) {
                sightsInView.add(sight.key)
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
        var playerViewDirection = playerLocation!!.bearing
        if (playerViewDirection > 180) {
            playerViewDirection -= 360
        }

        val opposite: Float = if (playerViewDirection < 0) {
            playerViewDirection + 180
        } else {
            playerViewDirection + 180
        }

        val leftNeighbors = mutableMapOf<Sight, Float>()
        for (sight in neighbors) {
            val heading = computeHeading(
                    LatLng(playerLocation.latitude, playerLocation.longitude),
                    sight.key.geoPosition
            )

            if (playerViewDirection < 0) {
                if (-180 < heading && heading < playerViewDirection
                || opposite < heading && heading < 180) {
                    leftNeighbors[sight.key] = sight.value
                }
            } else {
                if (opposite < heading && heading < playerViewDirection) {
                    leftNeighbors[sight.key] = sight.value
                }
            }
        }
        return leftNeighbors.toList().slice(0 until min(6, leftNeighbors.size)).toMap()
    }

    fun getRightNearSights(player: Player, neighbors: Map<Sight, Float>): Map<Sight, Float> {
        val playerLocation = player.geoLocation
        var playerViewDirection = playerLocation!!.bearing
        if (playerViewDirection > 180) {
            playerViewDirection -= 360
        }

        val opposite: Float = if (playerViewDirection < 0) {
            playerViewDirection + 180
        } else {
            playerViewDirection + 180
        }
        val rightNeighbors = mutableMapOf<Sight, Float>()
        for (sight in neighbors) {
            val heading = computeHeading(
                    LatLng(playerLocation.latitude, playerLocation.longitude),
                    sight.key.geoPosition
            )

            if (playerViewDirection < 0) {
                if (playerViewDirection < heading && heading < opposite) {
                    rightNeighbors[sight.key] = sight.value
                }
            } else {
                if (-180 < heading && heading < opposite
                        || playerViewDirection < heading && heading < 180) {
                    rightNeighbors[sight.key] = sight.value
                }
            }
        }
        return rightNeighbors.toList().slice(0 until min(6, rightNeighbors.size)).toMap()
    }

    private fun getAperture(distance: Float): Float {
        val apertureInRadians = Math.atan(50 / distance.toDouble())
        return Math.toDegrees(apertureInRadians).toFloat()
    }
}