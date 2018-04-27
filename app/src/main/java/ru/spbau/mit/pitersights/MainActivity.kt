package ru.spbau.mit.pitersights

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.SupportMapFragment

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
        , LoadingFragment.OnFragmentInteractionListener
        , MenuFragment.OnMenuFragmentInteractionListener
        , MapFragment.OnFragmentInteractionListener
{
    private val LOG_TAG = "MainActivity"
    private var loadingFragment: LoadingFragment = LoadingFragment()
    private var menuFragment: MenuFragment = MenuFragment()
    private var mapFragment: MapFragment = MapFragment()

    private var lastFragment: Fragment? = null

    override fun onFragmentInteraction(uri: Uri) {
        Log.d(LOG_TAG, "onLoadingFragmentInteraction")
    }

    override fun onMenuFragmentInteraction(uri: Uri) {
        Log.d(LOG_TAG, "onMenuFragmentInteraction")
    }

    override fun onMapFragmentInteraction(uri: Uri) {
        Log.d(LOG_TAG, "onMapFragmentInteraction")
    }

    override fun gotoPhoto() {
        Log.d(LOG_TAG, "Going to Photo")
    }

    override fun gotoMap() {
        Log.d(LOG_TAG, "Going to Map")
    }

    override fun gotoHistory() {
        Log.d(LOG_TAG, "Going to History")
    }

    override fun gotoAbout() {
        Log.d(LOG_TAG, "Going to About")
    }

    private fun setFragment(fragment: Fragment, containerId: Int = R.id.container) {
        if (fragment == lastFragment)
            return
        getSupportFragmentManager().beginTransaction().replace(containerId, fragment).commit()
        lastFragment = fragment
        Log.d("MainActivity", "setting fragment: " + fragment.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        setFragment(mapFragment, R.id.container)
        setFragment(menuFragment, R.id.menu_buttons_container)
    }
}
