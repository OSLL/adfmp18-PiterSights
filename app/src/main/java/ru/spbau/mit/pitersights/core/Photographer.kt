package ru.spbau.mit.pitersights.core

import java.io.File

class Photographer(private val storagePath: String) {
    fun takePhoto(sight: Sight): File {
        // типа фоткает конкретный объект и сохраняет его во временный файл
        return File("tmpPath")
    }

    fun savePhoto(sight: Sight, file: File) {
        val pathForPhoto = storagePath + sight.id + ".png"
        file.renameTo(File(pathForPhoto))
        sight.photo = pathForPhoto
    }

    fun erasePhoto(file: File) = file.delete()
}