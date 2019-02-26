package com.zippyid.zippydroid.camera

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi

import com.zippyid.zippydroid.camera.helpers.CameraSourcePreview
import com.zippyid.zippydroid.camera.helpers.GraphicOverlay
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import androidx.fragment.app.Fragment

import java.io.IOException
import java.lang.Exception

import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.network.model.DocumentType

import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.camera.helpers.GraphicFaceTrackerFactory
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.ByteArrayInputStream


class CameraFragment : Fragment() {
    companion object {
        private val TAG = "FaceTracker"
        private val RC_HANDLE_GMS = 9001
        private val RC_HANDLE_CAMERA_PERM = 2

        private const val CAMERA_MODE = "camera_mode"
        private const val DOCUMENT_TYPE = "document_type"

        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        fun newInstance(
            mode: ZippyActivity.CameraMode,
            documentType: DocumentType?
        ): CameraFragment {
            val bundle = Bundle()
            bundle.putSerializable(CameraFragment.CAMERA_MODE, mode)
            bundle.putParcelable(CameraFragment.DOCUMENT_TYPE, documentType)
            val fragment = CameraFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private var mCameraSource: CameraSource? = null
    private var mPreview: CameraSourcePreview? = null
    private var mGraphicOverlay: GraphicOverlay? = null

    private var cameraId = CameraSource.CAMERA_FACING_FRONT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onStart() {
        super.onStart()

        arguments = arguments
        val mode = arguments?.getSerializable(CameraFragment.CAMERA_MODE) as? ZippyActivity.CameraMode
            ?: throw IllegalArgumentException("Mode was not passed to CameraFragment!")
        val documentType = arguments?.getParcelable(CameraFragment.DOCUMENT_TYPE) as? DocumentType
            ?: throw IllegalArgumentException("Document type was not passed to CameraFragment!")

        if (mode == ZippyActivity.CameraMode.FACE) {
            showFaceFrame()
            cameraId = CameraSource.CAMERA_FACING_FRONT
        } else if (mode == ZippyActivity.CameraMode.DOCUMENT_FRONT) {
            showDocumentFrontFrame(documentType)
            cameraId = CameraSource.CAMERA_FACING_BACK
        } else if (mode == ZippyActivity.CameraMode.DOCUMENT_BACK) {
            showDocumentBackFrame(documentType)
            cameraId = CameraSource.CAMERA_FACING_BACK
        }

        mPreview = cameraSourcePreview as CameraSourcePreview
        mGraphicOverlay = faceOverlay as GraphicOverlay
        val rc = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource()
            takePictureBtn.setOnClickListener { takePhoto() }
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission")
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(activity!!, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }

        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(activity!!, permissions,
                RC_HANDLE_CAMERA_PERM)
        }

        Snackbar.make(mGraphicOverlay!!, "Access to the camera is needed for detection",
            Snackbar.LENGTH_INDEFINITE)
            .setAction("Ok", listener)
            .show()
    }

    private fun createCameraSource() {
        val detector = FaceDetector.Builder(context!!)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setMode(FaceDetector.ACCURATE_MODE)
            .build()
        detector.setProcessor(
            MultiProcessor.Builder<Face>(mGraphicOverlay?.let { GraphicFaceTrackerFactory(it) })
                .build())

        mCameraSource = CameraSource.Builder(context, detector)
            .setRequestedPreviewSize(640, 480)
            .setFacing(cameraId)
            .setRequestedFps(10.0f)
            .build()
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        mPreview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCameraSource != null) {
            mCameraSource!!.release()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode)
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source")
            createCameraSource()
            return
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.size +
                " Result code = " + if (grantResults.size > 0) grantResults[0] else "(empty)")

        val listener = DialogInterface.OnClickListener { dialog, id -> onStop() }

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Face Tracker sample")
            .setMessage("This application cannot run because it does not have the camera permission.  The application will now exit.")
            .setPositiveButton("Ok", listener)
            .show()
    }


    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            context!!)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(activity!!, code, RC_HANDLE_GMS)
            dlg.show()
        }

        if (mCameraSource != null) {
            try {
                mGraphicOverlay?.let { mPreview!!.start(mCameraSource!!, it) }
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }
        }
    }

    fun takePhoto() {
        var callback: CameraSource.PictureCallback = object: CameraSource.PictureCallback {

            override fun onPictureTaken(data: ByteArray) {
                try {
                    var loadedImage: Bitmap?
                    loadedImage = BitmapFactory.decodeByteArray(data, 0, data.size)

                    var exifInterface: ExifInterface?
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        var rotatedBitmap: Bitmap?
                        var byteArrayInputStream = ByteArrayInputStream(data)
                        exifInterface = ExifInterface(byteArrayInputStream)
                        var rotationDegrees = 0F
                        var orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> {
                                rotationDegrees = 90F
                            }
                            ExifInterface.ORIENTATION_ROTATE_180 -> {
                                rotationDegrees = 180F
                            }
                            ExifInterface.ORIENTATION_ROTATE_270 -> {
                                rotationDegrees = 270F
                            } }
                        var rotateMatrix = Matrix()
                        rotateMatrix.postRotate(rotationDegrees)
                        rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.width, loadedImage.height, rotateMatrix, false)
                        (activity as ZippyActivity).onCaptureCompleted(rotatedBitmap)
                    } else {
                        (activity as ZippyActivity).onCaptureCompleted(loadedImage)

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        mCameraSource!!.takePicture(null, callback)
    }

    private fun showFaceFrame() {
        faceFrameLl.visibility = View.VISIBLE
        documentFrontFrameLl.visibility = View.INVISIBLE
        documentBackFrameLl.visibility = View.INVISIBLE

        titleTv.text = ""
        descriptionTv.text = ""
    }

    private fun showDocumentFrontFrame(documentType: DocumentType) {
        faceFrameLl.visibility = View.INVISIBLE
        documentFrontFrameLl.visibility = View.VISIBLE
        documentBackFrameLl.visibility = View.INVISIBLE

        when (documentType.value) {
            "passport" -> {
                titleTv.text = "Passport"
                descriptionTv.text = "Position your passport in the frame"
            }
            "drivers_licence" -> {
                titleTv.text = "Front of driver's license"
                descriptionTv.text = "Position the front of your license in the frame"
            }
            "id_card" -> {
                titleTv.text = "Front of ID card"
                descriptionTv.text = "Position the front of your ID card in the frame"
            }
        }
    }

    private fun showDocumentBackFrame(documentType: DocumentType) {
        faceFrameLl.visibility = View.INVISIBLE
        documentFrontFrameLl.visibility = View.INVISIBLE
        documentBackFrameLl.visibility = View.VISIBLE

        when (documentType.value) {
            "drivers_licence" -> {
                titleTv.text = "Back of driver's license"
                descriptionTv.text = "Position the back of your license in the frame"
            }
            "id_card" -> {
                titleTv.text = "Back of ID card"
                descriptionTv.text = "Position the back of your ID card in the frame"
            }
        }
    }
}