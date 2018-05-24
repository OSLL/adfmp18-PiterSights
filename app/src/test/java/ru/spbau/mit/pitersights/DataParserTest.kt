package ru.spbau.mit.pitersights

import com.google.gson.JsonObject
import org.json.JSONObject
import org.junit.Assert.*;
import org.junit.*;

class DataParserTest {
    val dataParser = DataParser() // no state.

    @Test // Failed on Kotlin's NULL bug with JSON object.
    fun simpleParseTest() {
        val LAT = 60.0025
        val LNG = 30.3736
        val REFERENCE = "reference"
        val PLACE_NAME = "place_name"
        val tmp = JSONObject().put("123", 1)

        val jsonLocation = JSONObject(mapOf(
                "lat" to LAT,
                "lng" to LNG
        ))
        val jsonGeometry = JSONObject(mapOf(
                "location" to jsonLocation
        ))

        val map = mapOf<String, Any?>(
                "name" to "AU",
                "vicinity" to "spb",
                "place_name" to PLACE_NAME,
                "reference" to REFERENCE,
                "geometry" to jsonGeometry
        )
        val jsonObject = JSONObject(map)

        val googlePlaceMap = dataParser.getPlace(jsonObject)
        assertEquals(LAT, googlePlaceMap.get("lat"))
        assertEquals(LNG, googlePlaceMap.get("lng"))
        assertEquals(REFERENCE, googlePlaceMap.get("reference"))
        assertEquals("AU", googlePlaceMap.get("place_name"))
        assertEquals("spb", googlePlaceMap.get("vicinity"))
    }

    @Test
    fun absentParseTest() {
        // TODO check "-NA-"s
    }

    // expected runTimeError on absent locations.
}