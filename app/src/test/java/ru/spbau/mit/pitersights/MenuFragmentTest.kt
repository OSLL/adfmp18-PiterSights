package ru.spbau.mit.pitersights

import android.content.Context
import android.view.View
import org.junit.Test
import org.junit.Assert.*;
import org.junit.Before

class MenuFragmentTest {
//    val mMockContext : Context = Context()

    var menuFragment = MenuFragment()

    object MenuFragmentListener : MenuFragment.OnMenuFragmentInteractionListener {
        var takePhotoClicked : Boolean = false

        override fun onTakePhotoButtonClicked() {
            takePhotoClicked = true
        }
    }

    @Before
    fun initFragment() {
        menuFragment = MenuFragment()
        MenuFragmentListener.takePhotoClicked = false
    }

    @Test
    fun simpleStateTest() {
        assertFalse(menuFragment.menuButtonClicked)
    }
}