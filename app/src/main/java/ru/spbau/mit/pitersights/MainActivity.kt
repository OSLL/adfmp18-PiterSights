package ru.spbau.mit.pitersights

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
        , MenuFragment.OnMenuFragmentInteractionListener
        , TestFragment.OnTestFragmentInteractionListener
{
    private var menuFragment: MenuFragment = MenuFragment()
    private var testFragment: TestFragment = TestFragment()

    private var lastFragment: Fragment? = null

    override fun onTestFragmentInteraction(uri: Uri) {
        Log.d("MainActivity", "onTestFragmentInteraction")
    }

    override fun onMenuFragmentInteraction(uri: Uri) {
        Log.d("MainActivity", "onMenuFragmentInteraction")
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

        setFragment(menuFragment, R.id.menu_buttons_container)
        setFragment(testFragment)
    }
}
