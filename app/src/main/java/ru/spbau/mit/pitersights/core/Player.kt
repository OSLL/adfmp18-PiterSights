package ru.spbau.mit.pitersights.core

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.location.LocationManager
import android.os.Build
import com.hoan.dsensor_master.DProcessedSensor
import com.hoan.dsensor_master.DSensorEvent
import com.hoan.dsensor_master.DSensorManager
import com.hoan.dsensor_master.interfaces.DProcessedEventListener
import java.util.*
import java.util.concurrent.TimeUnit


class Player(private val context: Context?, private val activity: Activity) :
        LocationListener,
        DProcessedEventListener {

    internal var geoLocation: Location? = null
    private var lastUpdateTime = Calendar.getInstance().time

    private val locationListeners = mutableListOf<PlayerLocationListener>()
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted: Boolean = false

    init {
        DSensorManager.startDProcessedSensor(
                context,
                DProcessedSensor.TYPE_3D_COMPASS,
                this)
        getLocationPermission()
        getPlayerLocation()
    }

    fun registerLocationListener(listener: PlayerLocationListener) {
        locationListeners.add(listener)
    }

    fun unregisterLocationListener(listener: PlayerLocationListener) {
        locationListeners.remove(listener)
    }

    override fun onLocationChanged(location: Location?) {
        val currentTime = Calendar.getInstance().time
        val timeDiff = TimeUnit.MILLISECONDS.toSeconds(
                currentTime.time - lastUpdateTime.time
        )
        if (timeDiff > 1) {
            geoLocation!!.longitude = location!!.longitude
            geoLocation!!.latitude = location.latitude
            lastUpdateTime = currentTime
            for (l in locationListeners) {
                l.onPlayerLocationChanged()
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    override fun onProcessedValueChanged(dSensorEvent: DSensorEvent?) {
        val angleInRadians = dSensorEvent!!.values[0]
        val angleInDegrees = Math.toDegrees(angleInRadians.toDouble())
        if (geoLocation == null) {
            Log.w("PLAYER", "geoLocation is unexpectedly null")
            getPlayerLocation()
        }
        geoLocation!!.bearing = angleInDegrees.toFloat()
        for (l in locationListeners) {
            l.onPlayerLocationChanged()
        }
    }

    private fun getPlayerLocation() {
        try {
            val locationContext = Context.LOCATION_SERVICE
            val locationManager = context!!.getSystemService(locationContext) as LocationManager
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                locationManager.requestLocationUpdates(
                        provider,
                        1000L,
                        0.0f,
                        this)
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    geoLocation = location
                }
            }

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun getLocationPermission() {
        if (context?.let {
                    ActivityCompat.checkSelfPermission(it,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                } == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            activity.requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)

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

    interface PlayerLocationListener {
        fun onPlayerLocationChanged()
    }
}
