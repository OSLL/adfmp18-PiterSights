package ru.spbau.mit.pitersights

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import com.google.android.cameraview.CameraView;
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import android.os.HandlerThread
import android.support.v4.content.ContextCompat
import android.os.Build
import android.support.annotation.NonNull
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_camera.*

class CameraViewFragment: Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {
    private var mCameraView: CameraView? = null
    private val REQUEST_CAMERA_PERMISSION = 1

    private var mBackgroundHandler: Handler? = null

    private var leftSights = emptyArray<TextView>()
    private var rightSights = emptyArray<TextView>()

    private val mCallback = object : CameraView.Callback() {

        override fun onCameraOpened(cameraView: CameraView) {
            Log.d(this.toString(), "onCameraOpened")
        }

        override fun onCameraClosed(cameraView: CameraView) {
            Log.d(this.toString(), "onCameraClosed")
        }

        override fun onPictureTaken(cameraView: CameraView, data: ByteArray) {
            // TODO neater fix.
            val textureView = cameraView.getChildAt(0) as TextureView
            val bitmap = textureView.getBitmap()
            callPreviewDialog(bitmap, data)
        }
    }

    private val mOnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.camera -> if (mCameraView != null) {
                val isGeographerSaidYes = true // тут должны быть данные о достопримечательности, на которую мы смотрим
                if (isGeographerSaidYes) {
                    showDescription("Text description of sight must be here!")
                }
            }
        }
    }

    private fun showDescription(text: String) {
        val alert =  AlertDialog.Builder(requireContext()).setMessage(text).setNegativeButton(
                R.string.textDialogClose, DialogInterface.OnClickListener() { dialog, which ->
                    dialog.dismiss();
                }).show()

        val textView = alert.findViewById<TextView>(android.R.id.message)
        textView?.setTextColor(Color.WHITE);
        textView?.setTextSize(19F);
        textView?.setGravity(Gravity.CENTER);
        alert.window.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun callPreviewDialog(bitmap: Bitmap, data: ByteArray) {
        val previewDialog = PreviewDialogFragment(bitmap, data, this)
        previewDialog.show(this.requireFragmentManager(), "preview")
    }

    fun savePhoto(data: ByteArray) {
        Log.d(this.toString(), "onPictureTaken " + data.size)
        getBackgroundHandler().post(Runnable {
            val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "picture.jpg")
            var os: OutputStream? = null
            try {
                os = FileOutputStream(file)
                os.write(data)
                os.close()
            } catch (e: IOException) {
                Log.w(this.toString(), "Cannot write to $file", e)
            } finally {
                if (os != null) {
                    try {
                        os.close()
                    } catch (e: IOException) {
                        Log.w(this.toString(), "Error while closing FileOutputStream", e)
                    }
                }
            }
        })
    }

    private fun getBackgroundHandler(): Handler {
        if (mBackgroundHandler == null) {
            val thread = HandlerThread("background")
            thread.start()
            mBackgroundHandler = Handler(thread.looper)
        }
        return mBackgroundHandler as Handler
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mCameraView = camera as CameraView
        mCameraView!!.addCallback(mCallback)

        // тут нужно получить от географа ближайшие достопримечательности и внести их в
        // leftSights и rightSights
        leftSights.map { sight -> leftNeighbors.addView(sight) }
        rightSights.map { sight -> rightNeighbors.addView(sight) }

        // добавить обработчик на клики по rightSights и leftSights

        mCameraView!!.setOnClickListener(mOnClickListener)

    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this.requireActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView?.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this.requireActivity(),
                        Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment(R.string.camera_permission_confirmation,
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(this.requireFragmentManager(), "dialog");
        } else {
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    override fun onPause() {
        mCameraView?.stop()
        super.onPause()
    }

    override fun onDestroy() {
        // нужно что-то делать с поворотом
        super.onDestroy()
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler!!.looper.quitSafely()
            } else {
                mBackgroundHandler!!.looper.quit()
            }
            mBackgroundHandler = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>,
                                            @NonNull grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (permissions.size != 1 || grantResults.size != 1) {
                    throw RuntimeException("Error on requesting camera permission.")
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this.requireContext(), R.string.camera_permission_not_granted,
                            Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun takePicture() {
        Log.i("CameraViewFragment", "TakingPicture")
        var geographerSaidYes = true // проверка геолокации и компаса должна быть здесь
        if (geographerSaidYes) {
            mCameraView!!.takePicture() // имя фото тоже надо передавать
        } else {
            Toast.makeText(this.requireContext(), R.string.take_photo_not_allowed,
                    Toast.LENGTH_SHORT).show()
        }
    }
}