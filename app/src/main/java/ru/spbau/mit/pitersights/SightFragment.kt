package ru.spbau.mit.pitersights

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_sight.*;
import ru.spbau.mit.pitersights.core.Sight


class SightFragment : Fragment() {
    private var listener: OnSightFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sight, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSight(arguments!!.getParcelable("sight") as Sight)
    }

//    fun onButtonPressed(uri: Uri) {
//        listener?.onSightFragmentInteraction(uri)
//    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSightFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnLoadingFragmentInteractionListener")
        }
    }

    private fun setSight(sight: Sight) {
        sight_preview_label.text = sight.name
        sight_preview_description.text = sight.getFullDescription()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnSightFragmentInteractionListener {
        fun onSightFragmentInteraction(uri: Uri)
    }
}
