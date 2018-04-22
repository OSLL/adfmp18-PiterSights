package ru.spbau.mit.pitersights

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
        , BlankFragment.OnBlankFragmentInteractionListener
        , TestFragment.OnTestFragmentInteractionListener
{
    private var blankFragment: BlankFragment = BlankFragment()
    private var testFragment: TestFragment = TestFragment()

    private var lastFragment: Fragment? = null

    override fun onTestFragmentInteraction(uri: Uri) {
        Log.i("MainActivity", "onTestFragmentInteraction")
    }

    override fun onBlankFragmentInteraction(uri: Uri) {
        Log.i("MainActivity", "onBlankFragmentInteraction")
    }

    private fun setFragment(fragment: Fragment) {
        if (fragment == lastFragment)
            return
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit()
        lastFragment = fragment
        Log.i("MainActivity", "setting fragment: " + fragment.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        setFragment(testFragment)

        button.setOnClickListener({
            setFragment(testFragment)
        })

        button2.setOnClickListener({
            setFragment(blankFragment)
        })
    }
}
