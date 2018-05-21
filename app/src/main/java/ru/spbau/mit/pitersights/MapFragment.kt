package ru.spbau.mit.pitersights

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import ru.spbau.mit.pitersights.core.Player
import ru.spbau.mit.pitersights.core.Sight
import java.util.concurrent.TimeUnit

class MapFragment : Fragment(), OnMapReadyCallback, Player.PlayerLocationListener {
    var sights: List<Sight> = emptyList()
    var player: Player? = null

    private var mMap: GoogleMap? = null
    public var isReady = false
    private lateinit var mCameraPosition: CameraPosition
    private var playerMarker: Marker? = null

    private var mGeoDataClient: GeoDataClient? = null
    private var mPlaceDetectionClient: PlaceDetectionClient? = null
    private var mGeoApiContext: GeoApiContext? = null

    private val DEFAULT_ZOOM = 15

    private val PROXIMITY_RADIUS = 10000
    private var mCurrentRoute: Polyline? = null

    private val KEY_CAMERA_POSITION = "camera_position"
    private var mPlaceLatLngs: Array<LatLng?>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        mGeoDataClient = context?.let { Places.getGeoDataClient(it) }
        mPlaceDetectionClient = context?.let { Places.getPlaceDetectionClient(it) }
        mGeoApiContext = GeoApiContext()
                .setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_directions_api_key))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_CAMERA_POSITION, mMap!!.cameraPosition)
        super.onSaveInstanceState(outState)
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isReady = true
        mMap!!.uiSettings.isMapToolbarEnabled = false

        mMap!!.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                synchronized(this) {
                    val infoWindow = layoutInflater.inflate(R.layout.map_sight_description, null)

                    val title = infoWindow.findViewById(R.id.tv_title) as TextView
                    title.text = marker.title

                    val snippet = infoWindow.findViewById(R.id.tv_subtitle) as TextView
                    snippet.text = marker.snippet

                    val dest = marker.position
                    var selfLocation = player?.geoLocation
                    while (selfLocation == null) {
                        selfLocation = player?.geoLocation
                    }
                    if (dest != null) {
                        val result = DirectionsApi.newRequest(mGeoApiContext)
                                .mode(TravelMode.WALKING)
                                .origin(com.google.maps.model.LatLng(selfLocation.latitude, selfLocation.longitude))
                                .destination(com.google.maps.model.LatLng(dest.latitude, dest.longitude))
                                .await()

                        val decodedPath = PolyUtil.decode(
                                result.routes[0].overviewPolyline.encodedPath
                        )
                        mCurrentRoute?.remove()
                        mCurrentRoute = mMap!!.addPolyline(PolylineOptions()
                                .width(8.0f)
                                .color(resources.getColor(R.color.routeColor, null))
                                .addAll(decodedPath))
                    }

                    return infoWindow
                }
            }
        })

        updatePlayerLocation()
        showCurrentPlace()
//        updateLocationUI()
    }

    override fun onPlayerLocationChanged() {
        if (mMap != null) {
            updatePlayerLocation()
        }
    }

    private fun updatePlayerLocation() {
        synchronized(this) {
            try {
                var location = player?.geoLocation
                while (location == null) {
                    location = player?.geoLocation
                }
                if (playerMarker == null) {
                    playerMarker = mMap!!.addMarker(
                            MarkerOptions()
                                    .position(LatLng(location.latitude, location.longitude))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
                                    .snippet("Вы здесь")
                                    .rotation(location.bearing)
                                    .flat(true)
                    )
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude,
                                location.longitude), DEFAULT_ZOOM.toFloat()))
                } else {
                    playerMarker!!.position = LatLng(location.latitude, location.longitude)
                    playerMarker!!.rotation = location.bearing
                }

//            showCurrentPlace()
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message)
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        for (sight in sights) {
            val markerOptions = MarkerOptions()
            val placeName = sight.name
            val latLng = sight.geoPosition
            markerOptions.position(latLng)
            markerOptions.title(placeName)
            markerOptions.snippet(sight.getMapDescription())
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.star_filled))
            mMap!!.addMarker(markerOptions)
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
                mMap!!.addMarker(likelyPlace?.let {
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
//            if (mLocationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = true
//            } else {
//                mMap.isMyLocationEnabled = false
//                mMap.uiSettings.isMyLocationButtonEnabled = false
//                getLocationPermission()
//            }
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
        if (context !is OnFragmentInteractionListener) {
            throw RuntimeException(context.toString() + " must implement OnLoadingFragmentInteractionListener")
        }
        player!!.registerLocationListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        player!!.unregisterLocationListener(this)
    }

    interface OnFragmentInteractionListener {
        fun onMapFragmentInteraction(uri: Uri)
    }
}