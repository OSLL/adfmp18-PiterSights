package ru.spbau.mit.pitersights.core

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.location.LocationManager





class Player(private val context: Context?) {
    internal var geoLocation: Location? = null

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted: Boolean = false

    init {
        getLocationPermission()
        getPlayerLocation()
    }

    private fun getPlayerLocation() {
        try {
            val locationContext = Context.LOCATION_SERVICE
            val locationManager = context!!.getSystemService(locationContext) as LocationManager
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                locationManager.requestLocationUpdates(provider, 1000L, 0.0f,
                        object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                geoLocation = location
                            }

                            override fun onProviderDisabled(provider: String) {}

                            override fun onProviderEnabled(provider: String) {}

                            override fun onStatusChanged(provider: String, status: Int,
                                                extras: Bundle) {
                            }
                        })
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    geoLocation = location
                }
            }

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    private fun getLocationPermission() {
        if (context?.let {
                    ActivityCompat.checkSelfPermission(it,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                } == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
//            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }


    fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }
}
