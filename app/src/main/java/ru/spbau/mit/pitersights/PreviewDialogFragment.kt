package ru.spbau.mit.pitersights

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.support.annotation.NonNull
import android.widget.ImageView
import android.widget.Toast


class PreviewDialogFragment(): DialogFragment() {
    private val ARG_BITMAP = "bitmap"
    private val ARG_DATA = "bytearray"
    private val ARG_CANCEL = "cancel_message"

    @SuppressLint("ValidFragment")
    constructor(image: Bitmap, data: ByteArray) : this() {
        val fragment = PreviewDialogFragment()
        val args = Bundle()
        args.putParcelable(ARG_BITMAP, image)
        args.putByteArray(ARG_DATA, data)
        args.putString(ARG_CANCEL, "Photo is rejected")
        fragment.arguments = args
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
        val bitmapimage = args!!.getParcelable<Bitmap>(ARG_BITMAP)

        val imageView = ImageView(context)
        imageView.setImageBitmap(bitmapimage)

        return AlertDialog.Builder(activity)
                .setView(imageView)
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                    (parentFragment as CameraViewFragment).savePhoto(args.getByteArray(ARG_DATA))
                    Toast.makeText(activity, R.string.picture_taken, Toast.LENGTH_SHORT)
                            .show()
                })
                .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener { dialog, which ->
                    Toast.makeText(activity, args.getString(ARG_CANCEL), Toast.LENGTH_SHORT).show()
                })
                .create()
    }
}