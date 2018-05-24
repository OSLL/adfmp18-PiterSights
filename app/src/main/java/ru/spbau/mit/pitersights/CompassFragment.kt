package ru.spbau.mit.pitersights

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import ru.spbau.mit.pitersights.core.Player

class CompassFragment: Fragment(), Player.PlayerLocationListener {
    internal var player: Player? = null
    private var compassView: ImageView? = null
    private var currentAzimuth = 0.0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.compass, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        compassView = view.findViewById(R.id.compass)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        player!!.registerLocationListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        player!!.unregisterLocationListener(this)
    }

    override fun onPlayerLocationChanged() {
        val location = player!!.geoLocation
        val azimuth = location!!.bearing
        val compassAnimation = RotateAnimation(currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f)
        currentAzimuth = -azimuth

        compassAnimation.duration = 500
        compassAnimation.repeatCount = 0
        compassAnimation.fillAfter = true

        compassView?.startAnimation(compassAnimation)
    }

    interface OnCompassFragmentInteractionListener {
        fun onCompassFragmentInteraction(uri: Uri)
    }
}