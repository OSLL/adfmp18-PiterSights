package ru.spbau.mit.pitersights

import android.graphics.BitmapFactory
import android.widget.ImageView
import ru.spbau.mit.pitersights.core.Sight
import java.io.File

interface PhotoProvider {
    fun getPhotoDir() : File

    fun getPathForSight(sight: Sight) : String

    fun getFileForSight(sight: Sight) : File {
        return File(getPhotoDir(), getPathForSight(sight))
    }

    fun setImageOrLogo(view: ImageView, sight: Sight) {
        val photoFile = getFileForSight(sight)
        if (photoFile.exists()) {
            val photoBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            view.setImageBitmap(photoBitmap)
        } else {
            view.setImageResource(R.drawable.logo)
        }
    }
}
