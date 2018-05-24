package ru.spbau.mit.pitersights

import android.util.Log

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.HashMap

/**
 * Created by navneet on 23/7/16.
 */
class DataParser {
    fun parse(jsonData: String): List<HashMap<String, String>> {
        var jsonArray: JSONArray? = null
        val jsonObject: JSONObject

        try {
            Log.d("Places", "parse")
            jsonObject = JSONObject(jsonData)
            jsonArray = jsonObject.getJSONArray("results")
        } catch (e: JSONException) {
            Log.d("Places", "parse error")
            e.printStackTrace()
        }

        return getPlaces(jsonArray!!)
    }

    internal fun getPlaces(jsonArray: JSONArray): List<HashMap<String, String>> {
        val placesCount = jsonArray.length()
        val placesList = ArrayList<HashMap<String, String>>()
        var placeMap: HashMap<String, String>? = null
        Log.d("Places", "getPlaces")

        for (i in 0 until placesCount) {
            try {
                placeMap = getPlace(jsonArray.get(i) as JSONObject)
                placesList.add(placeMap)
                Log.d("Places", "Adding places")

            } catch (e: JSONException) {
                Log.d("Places", "Error in Adding places")
                e.printStackTrace()
            }

        }
        return placesList
    }

    internal fun getPlace(googlePlaceJson: JSONObject): HashMap<String, String> {
        val googlePlaceMap = HashMap<String, String>()
        var placeName = "-NA-"
        var vicinity = "-NA-"
        var latitude = ""
        var longitude = ""
        var reference = ""

        Log.d("getPlace", "Entered")

        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name")
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity")
            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat")
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng")
            reference = googlePlaceJson.getString("reference")
            googlePlaceMap["place_name"] = placeName
            googlePlaceMap["vicinity"] = vicinity
            googlePlaceMap["lat"] = latitude
            googlePlaceMap["lng"] = longitude
            googlePlaceMap["reference"] = reference
            Log.d("getPlace", "Putting Places")
        } catch (e: JSONException) {
            Log.d("getPlace", "Error")
            e.printStackTrace()
        }

        return googlePlaceMap
    }
}