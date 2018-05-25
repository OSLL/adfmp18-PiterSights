package ru.spbau.mit.pitersights

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import com.google.android.gms.maps.model.LatLng

import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.annotations.NotNull
import ru.spbau.mit.pitersights.core.Geographer
import ru.spbau.mit.pitersights.core.Player
import ru.spbau.mit.pitersights.core.Sight
import java.io.*
import android.graphics.Bitmap



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


//    https://developer.android.com/topic/performance/graphics/load-bitmap
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun decodeSampledBitmapFromFile(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val photoBitmap = BitmapFactory.decodeFile(filePath, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    }

    override fun setImageOrLogo(view: ImageView, sight: Sight, isPreview: Boolean) {
        val photoFile = getFileForSight(sight)
        if (photoFile.exists()) {
            val options = BitmapFactory.Options()
            if (isPreview) {
                // decodeSampledBitmapFromFile
                val reqWidth = resources.getDimension(R.dimen.history_preview_image_size).toInt();
                val reqHeight = resources.getDimension(R.dimen.history_preview_image_size).toInt();

                // First decode with inJustDecodeBounds=true to check dimensions
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(photoFile.absolutePath, options)

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false
            }
            val photoBitmap = BitmapFactory.decodeFile(photoFile.absolutePath, options)
            Log.d("HISTORY", "Bitmap: ${photoBitmap.width}, ${photoBitmap.height}.")
            view.setImageBitmap(photoBitmap)
        } else {
            view.setImageResource(R.drawable.logo)
        }
    }

    override fun getPathForSight(sight: Sight): String {
        return "photo#" + sight.id + ".jpg"
    }

    private fun setFragment(fragment: Fragment, containerId: Int = R.id.container, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction().replace(containerId, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
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

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
