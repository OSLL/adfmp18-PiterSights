package ru.spbau.mit.pitersights

import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class UrlHandler {
    fun handleUrl(strUrl: String): String {
        var data = ""
        var inputStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()
            inputStream = urlConnection.inputStream

            val bufferedReader = BufferedReader(InputStreamReader(inputStream!!))
            val stringBuffer = StringBuffer()

            var line = bufferedReader.readLine()
            while (line != null) {
                stringBuffer.append(line)
                line = bufferedReader.readLine()
            }
            data = stringBuffer.toString()
            bufferedReader.close()
        } catch (exception: Exception) {
            Log.d("Exception", exception.toString())
        } finally {
            inputStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }
}