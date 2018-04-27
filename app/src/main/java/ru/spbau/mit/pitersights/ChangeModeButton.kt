package ru.spbau.mit.pitersights

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton

class ChangeModeButton (val button: ImageButton, val photoModeResource: Int, val mapModeResource: Int) {
    enum class Mode {
        PHOTO,
        MAP,
        INVALID // set to be invisible and non-reachable.
    }

    var mode: Mode = Mode.INVALID

    fun setPhotoMode() {
        mode = Mode.PHOTO
        button.setImageResource(photoModeResource)
    }

    fun setMapMode() {
        mode = Mode.MAP
//        button.setBackgroundResource(mapModeResource)
        button.setImageResource(mapModeResource)
    }

    fun hide() {
        assert(mode == Mode.PHOTO || mode == Mode.MAP)
        button.visibility = GONE
    }

    fun show() {
        assert(mode == Mode.PHOTO || mode == Mode.MAP)
        button.visibility = VISIBLE
    }
}