package ru.spbau.mit.pitersights

import android.os.AsyncTask
import android.util.Log

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.HashMap

class NearbyPlacesGetter : AsyncTask<Any, String, String>() {

    private lateinit var googlePlaces: String
    private lateinit var mMap: GoogleMap
    private lateinit var url: String

    override fun doInBackground(vararg params: Any): String {
        try {
            mMap = params[0] as GoogleMap
            url = params[1] as String
            val urlHandler = UrlHandler()
            googlePlaces = urlHandler.handleUrl(url)
        } catch (e: Exception) {
            Log.d("GooglePlacesReadTask", e.toString())
        }

        return googlePlaces
    }

    override fun onPostExecute(result: String) {
        val dataParser = DataParser()
        val nearbyPlacesList = dataParser.parse(result)
        showNearbyPlaces(nearbyPlacesList)
    }

    private fun showNearbyPlaces(nearbyPlacesList: List<HashMap<String, String>>) {
        for (i in nearbyPlacesList.indices) {
            val markerOptions = MarkerOptions()
            val googlePlace = nearbyPlacesList[i]
            val lat = java.lang.Double.parseDouble(googlePlace["lat"])
            val lng = java.lang.Double.parseDouble(googlePlace["lng"])
            val placeName = googlePlace["place_name"]
            val latLng = LatLng(lat, lng)
            markerOptions.position(latLng)
            markerOptions.title("$placeName")
            markerOptions.snippet("The sight description")
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.star_filled))
            mMap.addMarker(markerOptions)
        }
    }
}
