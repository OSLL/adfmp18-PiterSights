package ru.spbau.mit.pitersights

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.model.LatLng

import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.annotations.NotNull
import ru.spbau.mit.pitersights.core.Geographer
import ru.spbau.mit.pitersights.core.Player
import ru.spbau.mit.pitersights.core.Sight
import java.io.*

class MainActivity : AppCompatActivity()
        , LoadingFragment.OnLoadingFragmentInteractionListener
        , MenuFragment.OnMenuFragmentInteractionListener
        , MapFragment.OnFragmentInteractionListener
        , HistoryFragment.OnHistoryFragmentInteractionListener
        , SightFragment.OnSightFragmentInteractionListener
        , PhotoProvider
        , SightsChangedListener
{
    private val LOG_TAG = "MainActivity"

    private var loadingFragment: LoadingFragment = LoadingFragment()
    private var menuFragment: MenuFragment = MenuFragment()
    private var mapFragment: MapFragment = MapFragment()
    private var historyFragment: HistoryFragment = HistoryFragment.newInstance(3)
    private var sightFragment: SightFragment = SightFragment()
    private var cameraViewFragment: CameraViewFragment = CameraViewFragment()

    private val SIGHTS_FOLDER = "sights"
    private var sights: List<Sight>? = null
    private var lastFragment: Fragment? = null
    private var player: Player? = null
    private var geographer: Geographer? = null

    fun getSights() = sights!!
    fun getPlayer() = player!!
    fun getGeographer() = geographer!!

    override fun onLoadingFragmentInteraction(uri: Uri) {
        Log.d(LOG_TAG, "onLoadingFragmentInteraction")
    }

    override fun onTakePhotoButtonClicked() {
        cameraViewFragment.takePicture()
    }

    override fun onSightsChanged() {
        historyFragment.sights = sights!!
    }

    override fun onMapFragmentInteraction(uri: Uri) {
        Log.d(LOG_TAG, "onMapFragmentInteraction")
    }

    override fun onHistoryFragmentInteraction(sight: Sight?) {
        Log.d("MainActivity", "onHistoryFragmentInteraction")
        val sightBundle = Bundle()
        sightBundle.putParcelable("sight", sight)
        sightFragment.arguments = sightBundle
        setFragment(sightFragment)
    }

    override fun onSightFragmentInteraction(uri: Uri) {
        Log.d(LOG_TAG, "onSightFragmentInteraction")
    }

    override fun gotoPhoto() {
        Log.d(LOG_TAG, "Going to Photo")
        title = getString(R.string.title_fragment_camera)
        setFragment(cameraViewFragment)
    }

    override fun gotoMap() {
        Log.d(LOG_TAG, "Going to Map")
        title = getString(R.string.title_fragment_map)
        setFragment(mapFragment)
    }

    override fun gotoHistory() {
        Log.d(LOG_TAG, "Going to History")
        title = getString(R.string.title_fragment_history)
        setFragment(historyFragment)
    }

    override fun gotoAbout() {
        Log.d(LOG_TAG, "Going to About")
    }

    override fun getPhotoDir() : File {
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    }

    override fun getPathForSight(sight: Sight): String {
        return "photo#" + sight.id + ".jpg"
    }

    private fun setFragment(fragment: Fragment, containerId: Int = R.id.container, addToBackStack: Boolean = true) {
        if (fragment == lastFragment)
            return
        val transaction = getSupportFragmentManager().beginTransaction().replace(containerId, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
        lastFragment = fragment
        Log.d("MainActivity", "setting fragment: " + fragment.toString())
    }

    fun initializeVariables() {
        if (sights == null) {
            val sightsFilenames = assets.list(SIGHTS_FOLDER)
            sights = sightsFilenames.map { sightFilename ->
                readSightFromFile(sightFilename)
            }
            player = Player(applicationContext, this)
            geographer = Geographer()
            geographer!!.sights = sights!!
        }
    }

    private fun readSightFromFile(sightFilename: String) :  Sight {
        val filename = "$SIGHTS_FOLDER/$sightFilename"

        val sightName: String = if (sightFilename.endsWith(".sight")) {
            sightFilename.substringBefore(".sight")
        } else {
            sightFilename
        }
        val inputStream = assets.open(filename)
        val bufferedReader = inputStream.bufferedReader()

        val getLine: () -> String = {
            bufferedReader.readLine()
        }

        val label = getLine()
        val label_ru = getLine()
        val label_en = getLine()
        val coordinatesLines = getLine().split(" ")
        val latitude = coordinatesLines[0].toDouble()
        val longitude = coordinatesLines[1].toDouble()
        val link = getLine()
        val splitter = getLine()
        assert(splitter.equals("==="))
        val shortDescription = readTextTillDelimiter(bufferedReader)
        val longDescription = bufferedReader.readText()
        val sight = Sight(sightName, label, shortDescription, longDescription, LatLng(latitude, longitude), link)
        val photoDir = getPhotoDir()
        val photoPath = getPathForSight(sight)
        val photoFile = File(photoDir, photoPath)
        if (photoFile.exists()) {
            sight.photo = photoPath
        }
        return sight
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initializeVariables()
        historyFragment.sights = sights!!

        setFragment(loadingFragment, R.id.container, false)
        setFragment(menuFragment, R.id.menu_buttons_container, false)
        gotoMap()
    }


    private fun readTextTillDelimiter(bufferedReader: BufferedReader) : String {
        var text = ""
        while (true) {
            if (!bufferedReader.ready()) {
                break
            }
            val line = bufferedReader.readLine()
            if (line == "===") {
                break
            }
            text += line + "\n"
        }
        return text
    }
}
