package ru.spbau.mit.pitersights

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.support.annotation.NonNull
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_camera.*


class PreviewDialogFragment(): DialogFragment() {
    private var cameraViewFragment: CameraViewFragment? = null
    private val ARG_BITMAP = "bitmap"
    private val ARG_DATA = "bytearray"
    private val ARG_CANCEL = "cancel_message"

    @SuppressLint("ValidFragment")
    constructor(image: Bitmap, data: ByteArray, cameraViewFragment: CameraViewFragment) : this() {
        this.cameraViewFragment = cameraViewFragment
        arguments = Bundle()
        val args = arguments!!
        args.putParcelable(ARG_BITMAP, image)
        args.putByteArray(ARG_DATA, data)
        args.putString(ARG_CANCEL, "Photo is rejected")
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments!!
        val bitmapImage = args.getParcelable<Bitmap>(ARG_BITMAP)

        val imageView = ImageView(context)
        imageView.setImageBitmap(bitmapImage)

        return AlertDialog.Builder(activity)
                .setView(imageView)
                .setPositiveButton(android.R.string.ok, { dialog, which ->
                    cameraViewFragment!!.savePhoto(args.getByteArray(ARG_DATA))
                    Toast.makeText(activity, R.string.picture_taken, Toast.LENGTH_SHORT).show()
                    (activity as SightsChangedListener).onSightsChanged()
                })
                .setNegativeButton(android.R.string.cancel, { dialog, which ->
                    Toast.makeText(activity, args.getString(ARG_CANCEL), Toast.LENGTH_SHORT).show()
                })
                .create()
    }
}