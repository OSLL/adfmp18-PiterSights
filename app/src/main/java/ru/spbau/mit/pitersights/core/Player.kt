package ru.spbau.mit.pitersights.core

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task

class Player(private val context: Context?) {
    internal val defaultLatLng = LatLng(-33.8523341, 151.2106085) // Sidney

    private var locationResult: Task<Location>? = null

    internal var geoLocation: Location? = null
//        get() {
//            return if (field != null) {
//                field
//            } else {
//                val defaultLocation = Location("default location")
//                defaultLocation.latitude = defaultLatLng.latitude
//                defaultLocation.longitude = defaultLatLng.longitude
//                defaultLocation.bearing = 10.0f
//                defaultLocation
//            }
//        }

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted: Boolean = false
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    init {
        mFusedLocationProviderClient = context?.let { LocationServices.getFusedLocationProviderClient(it) }
        getLocationPermission()
        getPlayerLocation()
    }


    private fun getPlayerLocation() {
        try {
            if (mLocationPermissionGranted) {
                locationResult = mFusedLocationProviderClient?.lastLocation
                locationResult!!.addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        geoLocation = task.result
                    }
                })
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
//        updateLocationUI()
    }
}
