package ru.spbau.mit.pitersights

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
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
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_camera.*
import ru.spbau.mit.pitersights.core.Geographer
import ru.spbau.mit.pitersights.core.Player
import ru.spbau.mit.pitersights.core.Sight

class CameraViewFragment(): Fragment(), ActivityCompat.OnRequestPermissionsResultCallback, Player.PlayerLocationListener {
    private var mCameraView: CameraView? = null
    private val REQUEST_CAMERA_PERMISSION = 1

    private var mBackgroundHandler: Handler? = null

    private var compassFragment = CompassFragment()

    private var geographer: Geographer? = null
    private var player: Player? = null

    private var leftNearSights = emptyMap<Sight, Float>()
    private var rightNearSights = emptyMap<Sight, Float>()
    private var nearSight: Sight? = null
    private @Volatile var isDescriptionOpened = false

    private val mCallback = object : CameraView.Callback() {

        override fun onCameraOpened(cameraView: CameraView) {
            Log.d(this.toString(), "onCameraOpened")
        }

        override fun onCameraClosed(cameraView: CameraView) {
            Log.d(this.toString(), "onCameraClosed")
        }

        override fun onPictureTaken(cameraView: CameraView, data: ByteArray) {
            val textureView = cameraView.getChildAt(0) as TextureView
            val bitmap = textureView.getBitmap()
            callPreviewDialog(bitmap, data)
        }
    }

    private val mOnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.camera -> if (mCameraView != null) {
                if (nearSight != null) {
                    showDescription(nearSight!!.name + "\n" + nearSight!!.getFullDescription())
                }
                if (isDescriptionOpened) {
                    isDescriptionOpened = false
                    val mShortDescription = shortDescription as TextView
                    mShortDescription.visibility = View.INVISIBLE
                }
            }
        }
    }

    internal inner class mOnTextViewListener(val sight: Sight) : View.OnClickListener {
        var size = Point()
        val display = activity!!.windowManager.defaultDisplay.getSize(size)

        override fun onClick(v: View) {
            val mShortDescription = shortDescription as TextView
            mShortDescription.text = sight.name + "\n" + sight.getShortDescription()

            val layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.topMargin = v.top + leftNeighbors.top
            layoutParams.leftMargin = leftNeighbors.right

            layoutParams.width = size.x - rightNeighbors.width - leftNeighbors.width
            mShortDescription.layoutParams = layoutParams
            if (!isDescriptionOpened) {
                isDescriptionOpened = true
                mShortDescription.visibility = View.VISIBLE
            }
        }

    }

    private fun showDescription(text: String) {
        val alert =  AlertDialog.Builder(requireContext()).setMessage(text).setNegativeButton(
                R.string.textDialogClose, DialogInterface.OnClickListener() { dialog, which ->
                    dialog.dismiss()
                }).show()

        val textView = alert.findViewById<TextView>(android.R.id.message)
        textView?.setTextColor(Color.WHITE)
        textView?.textSize = 16F
        textView?.gravity = Gravity.CENTER
        alert.window.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun callPreviewDialog(bitmap: Bitmap, data: ByteArray) {
        val previewDialog = PreviewDialogFragment(bitmap, data, this)
        previewDialog.show(this.requireFragmentManager(), "preview")
    }

    fun setGeographerAndPlayer(geographer: Geographer, player: Player) {
        this.geographer = geographer
        this.player = player
        this.player!!.registerLocationListener(this)
        compassFragment.player = this.player
    }

    override fun onPlayerLocationChanged() {
        var changeState = false
        if (this.isVisible && !isDescriptionOpened && geographer != null && player != null) {
            val neighbors = geographer!!.calculateDistance(player!!)
            val nearSight = geographer!!.detectSight(player!!, neighbors)
            if (nearSight != null) {
                neighbors.remove(nearSight)
            }
            val leftNearSights = geographer!!.getLeftNearSights(player!!, neighbors)
            val rightNearSights = geographer!!.getRightNearSights(player!!, neighbors)
            if (this.leftNearSights != leftNearSights) {
                this.leftNearSights = leftNearSights
                changeState = true
            }
            if (this.rightNearSights != rightNearSights) {
                this.rightNearSights = rightNearSights
                changeState = true
            }
            if (this.nearSight != nearSight) {
                this.nearSight = nearSight
                changeState = true
            }
        }
        if (changeState) {
            updateContent()
        }
    }

    private fun updateContent() {
        val mLeftNeighbors = leftNeighbors as LinearLayout
        mLeftNeighbors.removeAllViews()
        val mRightNeighbors = rightNeighbors as LinearLayout
        mRightNeighbors.removeAllViews()

        fun setView(sight: Sight, dist: Float, bearing: String): TextView {
            val view = TextView(requireContext())
            view.setText(dist.toInt().toString() + "m " + bearing)
            view?.setTextColor(Color.WHITE)
            view?.textSize = 16F
            view?.gravity = Gravity.CENTER
            view?.height = 100
            view?.setOnClickListener(mOnTextViewListener(sight))
            return view
        }

        leftNearSights.forEach { (key, value) ->
            mLeftNeighbors.addView(setView(key, value, "left"))
        }

        rightNearSights.forEach { (key, value) ->
            mRightNeighbors.addView(setView(key, value, "right"))
        }
    }

    fun savePhoto(data: ByteArray) {
        Log.d(this.toString(), "onPictureTaken " + data.size)
        getBackgroundHandler().post(Runnable {
            val interactionListener = activity as PhotoProvider
            val pathDir = interactionListener.getPhotoDir()
            val photoPath = interactionListener.getPathForSight(nearSight!!)
            nearSight!!.photo = photoPath
            val file = File(pathDir, photoPath)
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
                        nearSight!!.photo = photoPath
                        os.close()
                    } catch (e: IOException) {
                        Log.w(this.toString(), "Error while closing FileOutputStream", e)
                    }
                }
            }
            nearSight!!.photo = photoPath
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
        mCameraView!!.setOnClickListener(mOnClickListener)

        val mShortDescription = shortDescription as TextView
        mShortDescription.setTextColor(Color.WHITE)
        mShortDescription.textSize = 14F
        mShortDescription.gravity = Gravity.CENTER

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.compass_layout, compassFragment).commit()
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
        if (nearSight != null) {
            if (nearSight!!.photo.isEmpty()) {
                Log.i("CameraViewFragment", "TakingPicture")
                mCameraView!!.takePicture()
            } else {
                Toast.makeText(this.requireContext(), R.string.photo_taked_before,
                        Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this.requireContext(), R.string.take_photo_not_allowed,
                    Toast.LENGTH_SHORT).show()
        }
    }
}