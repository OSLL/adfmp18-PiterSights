package ru.spbau.mit.pitersights

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.TravelMode
import ru.spbau.mit.pitersights.core.Sight
import java.util.concurrent.TimeUnit

class MapFragment : Fragment(), OnMapReadyCallback {
    var sights: List<Sight> = emptyList()
    private lateinit var mMap: GoogleMap
    private lateinit var mCameraPosition: CameraPosition

    private var mGeoDataClient: GeoDataClient? = null
    private var mPlaceDetectionClient: PlaceDetectionClient? = null
    private var mGeoApiContext: GeoApiContext? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null

    private val mDefaultLocation = LatLng(-33.8523341, 151.2106085)
    private val DEFAULT_ZOOM = 15
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var mLocationPermissionGranted: Boolean = false

    private var mLastKnownLocation: Location? = null
    private val PROXIMITY_RADIUS = 10000
    private var mCurrentRoute: Polyline? = null

    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"
    private var mPlaceLatLngs: Array<LatLng?>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        mGeoDataClient = context?.let { Places.getGeoDataClient(it) }
        mPlaceDetectionClient = context?.let { Places.getPlaceDetectionClient(it) }
        mFusedLocationProviderClient = context?.let { LocationServices.getFusedLocationProviderClient(it) }
        mGeoApiContext = GeoApiContext()
                .setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_directions_api_key))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_CAMERA_POSITION, mMap.cameraPosition)
        outState.putParcelable(KEY_LOCATION, mLastKnownLocation)
        super.onSaveInstanceState(outState)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false

        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val infoWindow = layoutInflater.inflate(R.layout.map_sight_description, null)

                val title = infoWindow.findViewById(R.id.tv_title) as TextView
                title.text = marker.title

                val snippet = infoWindow.findViewById(R.id.tv_subtitle) as TextView
                snippet.text = marker.snippet

                val dest = marker.position
                val selfLocation = mLastKnownLocation
                if (selfLocation != null && dest != null) {
                    val result = DirectionsApi.newRequest(mGeoApiContext)
                            .mode(TravelMode.WALKING)
                            .origin(com.google.maps.model.LatLng(selfLocation.latitude, selfLocation.longitude))
                            .destination(com.google.maps.model.LatLng(dest.latitude, dest.longitude))
                            .await()

                    val decodedPath = PolyUtil.decode(
                            result.routes[0].overviewPolyline.encodedPath
                    )
                    mCurrentRoute?.remove()
                    mCurrentRoute = mMap.addPolyline(PolylineOptions()
                            .width(8.0f)
                            .color(resources.getColor(R.color.routeColor, null))
                            .addAll(decodedPath))
                }

                return infoWindow
            }
        })

        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
    }

    private fun getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient?.lastLocation
                locationResult!!.addOnCompleteListener({ task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.result
                        Log.i(tag, "current location: $mLastKnownLocation")
                        val location = mLastKnownLocation
                        if (location != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    LatLng(location.latitude,
                                            location.longitude), DEFAULT_ZOOM.toFloat()))

                         showCurrentPlace()
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


    private fun getLocationPermission() {
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


    override fun onRequestPermissionsResult(requestCode: Int,
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
        updateLocationUI()
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        if (mLocationPermissionGranted) {
            val currentLocation = mLastKnownLocation
            val url = getUrl(
                    currentLocation!!.latitude,
                    currentLocation.longitude,
                    "point_of_interest")
            val getNearbyPlacesData = NearbyPlacesGetter()
            getNearbyPlacesData.execute(mMap, url)
        } else {
            getLocationPermission()
        }
    }

    private fun getUrl(latitude: Double, longitude: Double, nearbyPlace: String): String {

        val googlePlacesUrl = StringBuilder(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
        )
        googlePlacesUrl.append("location=$latitude,$longitude")
        googlePlacesUrl.append("&radius=$PROXIMITY_RADIUS")
        googlePlacesUrl.append("&type=$nearbyPlace")
        googlePlacesUrl.append("&sensor=true")
        googlePlacesUrl.append("&key=" + "AIzaSyATuUiZUkEc_UgHuqsBJa1oqaODI-3mLs0")
        Log.d("getUrl", googlePlacesUrl.toString())
        return googlePlacesUrl.toString()
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private fun openPlacesDialog() {
        val currentPlaces = mPlaceLatLngs
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
        } catch (exception: SecurityException) {
            Log.e("Exception: %s", exception.message)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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
        fun onMapFragmentInteraction(uri: Uri)
    }
}