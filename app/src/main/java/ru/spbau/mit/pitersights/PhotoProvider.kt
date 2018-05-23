package ru.spbau.mit.pitersights

import ru.spbau.mit.pitersights.core.Sight
import java.io.File

interface PhotoProvider {
    fun getPhotoDir() : File

    fun getPathForSight(sight: Sight) : String

    fun getFileForSight(sight: Sight) : File {
        return File(getPhotoDir(), getPathForSight(sight))
    }
}
