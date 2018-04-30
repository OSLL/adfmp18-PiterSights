package ru.spbau.mit.pitersights

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import java.util.concurrent.TimeUnit

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var mMap: GoogleMap

    private lateinit var mCameraPosition: CameraPosition

    // The entry points to the Places API.
    private var mGeoDataClient: GeoDataClient? = null
    private var mPlaceDetectionClient: PlaceDetectionClient? = null

    private var mGeoApiContext: GeoApiContext? = null

    // The entry point to the Fused Location Provider.
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val mDefaultLocation = LatLng(-33.8523341, 151.2106085)
    private val DEFAULT_ZOOM = 15
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted: Boolean = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var mLastKnownLocation: Location? = null

    // Keys for storing activity state.
    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"

    // Used for selecting the current place.
    private val M_MAX_ENTRIES = 200
    private var mLikelyPlaceNames: Array<String?>? = null
    private var mLikelyPlaceAddresses: Array<String>? = null
    private var mLikelyPlaceAttributions: Array<String>? = null
    private var mLikelyPlaceLatLngs: Array<LatLng?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }

        // Construct a GeoDataClient.
        mGeoDataClient = context?.let { Places.getGeoDataClient(it) }

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = context?.let { Places.getPlaceDetectionClient(it) }

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = context?.let { LocationServices.getFusedLocationProviderClient(it) }

        mGeoApiContext = GeoApiContext()
                .setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_directions_api_key))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS)
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_CAMERA_POSITION, mMap.cameraPosition)
        outState.putParcelable(KEY_LOCATION, mLastKnownLocation)
        super.onSaveInstanceState(outState)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)

//        // Use a custom info window adapter to handle multiple lines of text in the
//        // info window contents.
//        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
//
//            override// Return null here, so that getInfoContents() is called next.
//            fun getInfoWindow(arg0: Marker): View? {
//                return null
//            }
//
//            override fun getInfoContents(marker: Marker): View {
//                // Inflate the layouts for the info window, title and snippet.
//                val infoWindow = layoutInflater.inflate(R.layout.custom_info_contents,
//                        findViewById(R.id.map) as FrameLayout, false)
//
//                val title = infoWindow.findViewById(R.id.title) as TextView
//                title.text = marker.title
//
//                val snippet = infoWindow.findViewById(R.id.snippet) as TextView
//                snippet.text = marker.snippet
//
//                return infoWindow
//            }
//        })

        // Prompt the user for permission.
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
        showCurrentPlace()
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient?.lastLocation
                locationResult!!.addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.result
                        val location = mLastKnownLocation
                        if (location != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    LatLng(location.latitude,
                                            location.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        Log.d(tag, "Current location is null. Using defaults.")
                        Log.e(tag, "Exception: %s", task.exception)
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM.toFloat()))
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                })
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (context?.let {
                    ActivityCompat.checkSelfPermission(it,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                } == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
//    @SuppressLint("MissingPermission")
    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            val placeResult = mPlaceDetectionClient?.getCurrentPlace(null)
            placeResult?.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result

                    // Set the count, handling cases where less than 5 entries are returned.
                    val count: Int
                    if (likelyPlaces.count < M_MAX_ENTRIES) {
                        count = likelyPlaces.count
                    } else {
                        count = M_MAX_ENTRIES
                    }

                    var i = 0
                    mLikelyPlaceNames = arrayOfNulls(count)
//                    mLikelyPlaceAddresses = arrayOfNulls(count)
//                    mLikelyPlaceAttributions = arrayOfNulls(count)
                    mLikelyPlaceLatLngs = arrayOfNulls(count)

                    for (placeLikelihood in likelyPlaces) {
                        // Build a list of likely places to show the user.
                        mLikelyPlaceNames?.set(i, placeLikelihood.place.name as String)
//                        (placeLikelihood.place
//                                .address as String??.).let { mLikelyPlaceAddresses?.set(i, it.toString()) }
//                        placeLikelihood.place
//                                .attributions as String??. let { mLikelyPlaceAttributions?.set(i, it) }
                        mLikelyPlaceLatLngs!![i] = placeLikelihood.place.latLng

                        i++
                        if (i > count - 1) {
                            break
                        }
                    }

                    // Release the place likelihood buffer, to avoid memory leaks.
                    likelyPlaces.release()

                    // Show a dialog offering the user the list of likely places, and add a
                    // marker at the selected place.
                    openPlacesDialog()

                } else {
                    Log.e(tag, "Exception: %s", task.exception)
                }
            }
        } else {
//            // The user has not granted permission.
//            Log.i(tag, "The user did not grant location permission.")
//
//            // Add a default marker, because the user hasn't selected a place.
//            mMap.addMarker(MarkerOptions()
//                    .title(getString(R.string.default_info_title))
//                    .position(mDefaultLocation)
//                    .snippet(getString(R.string.default_info_snippet)))
//
//            // Prompt the user for permission.
//            getLocationPermission()
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.showInfoWindow()
        val dest = marker?.position
        val selfLocation = mLastKnownLocation
//        val selfLocation = if (mLastKnownLocation != null) LatLng(mLastKnownLocation.latitude)
        if (selfLocation != null) {
            if (dest != null) {
                val result = DirectionsApi.newRequest(mGeoApiContext)
                        .mode(TravelMode.WALKING)
                        .origin(com.google.maps.model.LatLng(selfLocation.latitude, selfLocation.longitude))
                        .destination(com.google.maps.model.LatLng(dest.latitude, dest.longitude))
                        .await()

                val decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.getEncodedPath())
                mMap.addPolyline(PolylineOptions().addAll(decodedPath))
            }
        }
        return true
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private fun openPlacesDialog() {
        val currentPlaces = mLikelyPlaceLatLngs
        if (currentPlaces != null) {
            for (likelyPlace in currentPlaces) {
                mMap.addMarker(likelyPlace?.let {
                    MarkerOptions()
                            .title("some place")
                            .position(it)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.star_filled))
                            .snippet("This is the sight description")
                })
            }
        }
    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private fun updateLocationUI() {
        try {
            if (mLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
        } else {
            throw RuntimeException(context.toString() + " must implement OnLoadingFragmentInteractionListener")
        }
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onMapFragmentInteraction(uri: Uri)
    }
}