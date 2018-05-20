package ru.spbau.mit.pitersights.core

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.location.LocationManager
import android.location.LocationProvider
import android.net.Uri
import java.util.*
import java.util.concurrent.TimeUnit


class Player(private val context: Context?) : LocationListener, SensorEventListener {
    internal var geoLocation: Location? = null
    internal var accelerometer: Sensor? = null
    internal var magnetometer: Sensor? = null
    private val mLastAccelerometer = FloatArray(3)
    private val mLastMagnetometer = FloatArray(3)
    private var mLastAccelerometerSet = false
    private var mLastMagnetometerSet = false
    private val mR = FloatArray(9)
    private val mOrientation = FloatArray(3)
    private val mCurrentDegree = 0f
    private var lastUpdateTime = Calendar.getInstance().time


    internal val locationListeners = mutableListOf<PlayerLocationListener>()
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted: Boolean = false

    init {
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
            geoLocation = location
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


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        synchronized (this) {
            val currentTime = Calendar.getInstance().time
            val timeDiff = currentTime.time - lastUpdateTime.time
            val alpha = 0.97f
            if (event.sensor == accelerometer) {
                mLastAccelerometer[0] = alpha * mLastAccelerometer[0] + (1 - alpha) * event.values[0]
                mLastAccelerometer[1] = alpha * mLastAccelerometer[1] + (1 - alpha) * event.values[1]
                mLastAccelerometer[2] = alpha * mLastAccelerometer[2] + (1 - alpha) * event.values[2]
                mLastAccelerometerSet = true
            } else if (event.sensor == magnetometer) {
                mLastMagnetometer[0] = alpha * mLastMagnetometer[0] + (1 - alpha) * event.values[0]
                mLastMagnetometer[1] = alpha * mLastMagnetometer[1] + (1 - alpha) * event.values[1]
                mLastMagnetometer[2] = alpha * mLastMagnetometer[2] + (1 - alpha) * event.values[2]
                mLastMagnetometerSet = true
            }
            if (mLastAccelerometerSet && mLastMagnetometerSet) {

                SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer)
                SensorManager.getOrientation(mR, mOrientation)
                val azimuthInRadians = mOrientation[0]
                val azimuthInDegress = Math.toDegrees(azimuthInRadians.toDouble()) % 360
                geoLocation!!.bearing = azimuthInDegress.toFloat()
                for (l in locationListeners) {
                    l.onPlayerLocationChanged()
                }
                lastUpdateTime = currentTime
            }
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

                val sensorContext = Context.SENSOR_SERVICE
                val sensorManager = context.getSystemService(sensorContext) as SensorManager
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
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

    interface PlayerLocationListener {
        fun onPlayerLocationChanged()
    }
}
