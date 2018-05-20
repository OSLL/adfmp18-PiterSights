package ru.spbau.mit.pitersights

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_menu.*


class MenuFragment : Fragment() {
    private var listener: OnMenuFragmentInteractionListener? = null
    private var menuButtonClicked: Boolean = false
    private lateinit var changeModeButton: ChangeModeButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(this.toString(), "Created")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(this.toString(), "view created")

        val childrenButtons = arrayOf(photoButton, mapButton, historyButton, aboutButton)
        val hideButtons = {
            for (button in childrenButtons) {
                button.visibility = GONE
            }
            menuButtonClicked = false
        }

        val photoButtonListener = OnClickListener{
            preparePhotoMode()
            hideButtons()
            listener?.gotoPhoto()
        }
        photoButton.setOnClickListener(photoButtonListener)

        val mapButtonListener = OnClickListener{
            prepareNonPhotoMode()
            hideButtons()
            listener?.gotoMap()
        }
        mapButton.setOnClickListener(mapButtonListener)

        historyButton.setOnClickListener({
            prepareNonPhotoMode()
            hideButtons()
            listener?.gotoHistory()
        })

        aboutButton.setOnClickListener({
            prepareNonPhotoMode()
            hideButtons()
            listener?.gotoAbout()
        })

        if (!menuButtonClicked) {
            hideButtons()
        }

        menuButton.setOnClickListener({
            val newState = if (menuButtonClicked) {
                GONE
            } else {
                VISIBLE
            }
            Log.d(this.toString(), "menuButtonClicked")
            for (button in childrenButtons) {
                button.visibility = newState
            }
            menuButtonClicked = !menuButtonClicked;
        })

        changeModeButton = ChangeModeButton(
                activity!!.findViewById(R.id.changeModeButton),
                R.drawable.ic_menu_camera,
                R.drawable.ic_menu_mapmode)
        prepareNonPhotoMode()
        changeModeButton.button.setOnClickListener({
            when (changeModeButton.mode) {
                ChangeModeButton.Mode.PHOTO -> photoButtonListener.onClick(it)
                ChangeModeButton.Mode.MAP -> mapButtonListener.onClick(it)
                ChangeModeButton.Mode.INVALID -> throw RuntimeException("Invalid changeModeButton state.")
            }
        })
        takePhotoButton.setOnClickListener({
            listener!!.onTakePhotoButtonClicked()
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnMenuFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnTestFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun preparePhotoMode() {
        changeModeButton.setMapMode()
        takePhotoButton.visibility = VISIBLE
    }

    private fun prepareNonPhotoMode() {
        changeModeButton.setPhotoMode()
        takePhotoButton.visibility = GONE
    }

    interface OnMenuFragmentInteractionListener {
        fun onTakePhotoButtonClicked();

        fun gotoPhoto() {
            Log.d("menu_fragment", "Going to Photo")
        }

        fun gotoMap() {
            Log.d("menu_fragment", "Going to Map")
        }

        fun gotoHistory() {
            Log.d("menu_fragment", "Going to History")
        }

        fun gotoAbout() {
            Log.d("menu_fragment", "Going to About")
        }
    }
}
