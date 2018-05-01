package ru.spbau.mit.pitersights

import android.Manifest
import android.graphics.SurfaceTexture
import android.support.v4.app.Fragment
import android.content.Context.CAMERA_SERVICE
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.util.Size
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import java.util.*
import android.os.HandlerThread
import android.hardware.camera2.CameraMetadata
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.*
import kotlinx.android.synthetic.main.fragment_camera.*


class CameraViewFragment: Fragment() {
    private var mTextureView: TextureView? = null
    private var mPreviewSize: Size?= null
    private var mCameraDevice: CameraDevice? = null
    private var mPreviewBuilder: CaptureRequest.Builder? = null
    private var mPreviewSession: CameraCaptureSession? = null

    private val mSurfaceTextureListner = object: TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.i(this.toString(), "onSurfaceTextureSizeChanged()")
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            Log.i(this.toString(), "onSurfaceTextureUpdated()")
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            Log.i(this.toString(), "onSurfaceTextureDestroyed()")
            return false
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.i(this.toString(), "onSurfaceTextureAvailable()")
            val manager = context?.getSystemService(CAMERA_SERVICE) as CameraManager
            try {
                val cameraId = manager.cameraIdList[0]
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                mPreviewSize = map.getOutputSizes(SurfaceTexture::class.java)[0]

                val permission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)

                if (permission == PackageManager.PERMISSION_GRANTED) {
                    manager.openCamera(cameraId, mStateCallback, null)
                }
            } catch(e: CameraAccessException) {}
        }
    }

    private val mStateCallback = object: CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice?) {
            Log.i(this.toString(), "onOpened")
            mCameraDevice = camera
            val texture = mTextureView?.getSurfaceTexture()
            if (texture != null) {
                texture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
                val surface = Surface(texture)
                try {
                    mPreviewBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                } catch (e: CameraAccessException) {}
                mPreviewBuilder?.addTarget(surface)
                try {
                    mCameraDevice?.createCaptureSession(Arrays.asList(surface), mPreviewStateCallback, null);
                } catch (e: CameraAccessException) {}
            }

        }

        override fun onDisconnected(camera: CameraDevice?) {
            Log.e(this.toString(), "onDisconnected");
        }

        override fun onError(camera: CameraDevice?, error: Int) {
            Log.e(this.toString(), "onError")
        }
    }

    private val mPreviewStateCallback = object: CameraCaptureSession.StateCallback() {

        override fun onConfigureFailed(session: CameraCaptureSession?) {
            Log.e(this.toString(), "CameraCaptureSession Configure failed")
        }

        override fun onConfigured(session: CameraCaptureSession?) {
            Log.i(this.toString(), "onConfigured")
            mPreviewSession = session
            mPreviewBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            val backgroundThread = HandlerThread("CameraPreview")
            backgroundThread.start()
            val backgroundHandler = Handler(backgroundThread.looper)

            try {
                mPreviewSession?.setRepeatingRequest(mPreviewBuilder?.build(), null, backgroundHandler)
            } catch (e: CameraAccessException) {}
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTextureView = cameraView as TextureView
        mTextureView!!.setSurfaceTextureListener(mSurfaceTextureListner);
    }

    override fun onPause() {
        super.onPause()
        if (mCameraDevice != null) {
            mCameraDevice!!.close()
            mCameraDevice = null
        }

    }

    interface OnCameraFragmentInteractionListener {
        fun onCameraFragmentInteraction(uri: Uri)
    }
}