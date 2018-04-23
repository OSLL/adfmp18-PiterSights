package ru.spbau.mit.pitersights

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_menu.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MenuFragment.OnMenuFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MenuFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MenuFragment : Fragment() {
    private var listener: OnMenuFragmentInteractionListener? = null
    private var menuButtonClicked: Boolean = false

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
        if (!menuButtonClicked) {
            for (button in childrenButtons) {
                button.visibility = INVISIBLE
            }
        }

        menuButton.setOnClickListener({
            val newState = if (menuButtonClicked) {
                INVISIBLE
            } else {
                VISIBLE
            }
            Log.d(this.toString(), "menuButtonClicked")
            for (button in childrenButtons) {
                button.visibility = newState
            }
            menuButtonClicked = !menuButtonClicked;
        })
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onMenuFragmentInteraction(uri)
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnMenuFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onMenuFragmentInteraction(uri: Uri)
    }
}
