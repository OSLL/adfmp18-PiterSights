package ru.spbau.mit.pitersights

import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import android.view.Display
import android.view.Menu
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_menu.*

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    val appContext = InstrumentationRegistry.getTargetContext()

    object listener : MenuFragment.OnMenuFragmentInteractionListener {
//        override fun getApplicationContext(): Context {
//            appContext.applicationContext
//        }

        override fun onTakePhotoButtonClicked() {
            Log.d("TEST", "onTakePhotoButtonClicked")
        }
    }

    @Test
    fun useAppContext() {
        assertEquals("ru.spbau.mit.pitersights", appContext.packageName)

        val menuFragment = MenuFragment()
        menuFragment.listener = listener
//        menuFragment.onViewCreated(View(appContext), null)
//        assertEquals(menuFragment.changeModeButton.mode, ChangeModeButton.Mode.INVALID)
    }
}
