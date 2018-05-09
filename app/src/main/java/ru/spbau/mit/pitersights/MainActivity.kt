package ru.spbau.mit.pitersights

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_camera.*
import ru.spbau.mit.pitersights.core.Sight
import java.io.Serializable

class MainActivity : AppCompatActivity()
        , LoadingFragment.OnLoadingFragmentInteractionListener
        , MenuFragment.OnMenuFragmentInteractionListener
        , MapFragment.OnFragmentInteractionListener
        , HistoryFragment.OnHistoryFragmentInteractionListener
        , SightFragment.OnSightFragmentInteractionListener
//        , CameraViewFragment.OnCameraFragmentInteractionListener
{
    private val LOG_TAG = "MainActivity"

    private var loadingFragment: LoadingFragment = LoadingFragment()
    private var menuFragment: MenuFragment = MenuFragment()
    private var mapFragment: MapFragment = MapFragment()
    private var historyFragment: HistoryFragment = HistoryFragment.newInstance(3)
    private var sightFragment: SightFragment = SightFragment()
    private var cameraViewFragment: CameraViewFragment = CameraViewFragment()

    private var lastFragment: Fragment? = null

    override fun onLoadingFragmentInteraction(uri: Uri) {
        Log.d(LOG_TAG, "onLoadingFragmentInteraction")
    }

    override fun onMenuFragmentInteraction(uri: Uri) {
        Log.d(LOG_TAG, "onMenuFragmentInteraction")
    }

    override fun onMapFragmentInteraction(uri: Uri) {
        Log.d(LOG_TAG, "onMapFragmentInteraction")
    }

//    override fun onCameraFragmentInteraction(uri: Uri) {
//        Log.d(LOG_TAG, "onCameraFragmentInteraction")
//    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val COUNT = 25
        val description: String = resources.getString(R.string.sight_description)
        val sights: MutableList<Sight> = MutableList(COUNT,
                { _ -> Sight
                    Sight(resources.getString(R.string.sight_label)
                    , arrayListOf(description, description, description)
                    , R.drawable.logo
                    )
                }
        )
        historyFragment.sights = sights

        setFragment(loadingFragment, R.id.container, false)
        setFragment(menuFragment, R.id.menu_buttons_container, false)
        gotoPhoto()
    }
}
