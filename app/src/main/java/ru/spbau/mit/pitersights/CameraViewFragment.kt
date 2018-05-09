package ru.spbau.mit.pitersights

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.android.synthetic.main.fragment_menu.*


class CameraViewFragment: Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {
    private var mCameraView: CameraView? = null
    private val REQUEST_CAMERA_PERMISSION = 1

    private var mBackgroundHandler: Handler? = null

    private val mOnClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            when (v.getId()) {
                // можно ли фотографировать? добавить обработку
                R.id.takePhotoButton -> mCameraView?.takePicture()
            }
        }
    }

    private val mCallback = object : CameraView.Callback() {

        override fun onCameraOpened(cameraView: CameraView) {
            Log.d(this.toString(), "onCameraOpened")
        }

        override fun onCameraClosed(cameraView: CameraView) {
            Log.d(this.toString(), "onCameraClosed")
        }

        override fun onPictureTaken(cameraView: CameraView, data: ByteArray) {
            // предпросмотр, все дела
            Log.d(this.toString(), "onPictureTaken " + data.size)
            Toast.makeText(cameraView.context, R.string.picture_taken, Toast.LENGTH_SHORT)
                    .show()
            getBackgroundHandler().post(Runnable {
                val file = File(Environment.getExternalStorageDirectory(),
                        "picture.jpg")
                var os: OutputStream? = null
                try {
                    os = FileOutputStream(file)
                    os!!.write(data)
                    os!!.close()
                } catch (e: IOException) {
                    Log.w(this.toString(), "Cannot write to $file", e)
                } finally {
                    if (os != null) {
                        try {
                            os!!.close()
                        } catch (e: IOException) {
                        }

                    }
                }
            })
        }
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
        mCameraView?.addCallback(mCallback);
        takePhotoButton.setOnClickListener(mOnClickListener)
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
                mBackgroundHandler!!.getLooper().quitSafely()
            } else {
                mBackgroundHandler!!.getLooper().quit()
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
}