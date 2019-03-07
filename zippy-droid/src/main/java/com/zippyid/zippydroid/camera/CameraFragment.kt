package com.zippyid.zippydroid.camera

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.core.app.ActivityCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import androidx.fragment.app.Fragment
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException
import java.lang.Exception
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.network.model.DocumentType
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.camera.helpers.*
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.ByteArrayInputStream


class CameraFragment : Fragment(), GraphicFaceTracker.FaceDetectorListener, BarcodeTracker.BarcodeDetectorListener {
    override fun onFaceDetected(face: Face) {
        takePhoto()
    }
    override fun onBarcodeDetected(barcode: Barcode) {
        takePhoto()
    }

    companion object {
        private const val TAG = "FaceTracker"
        private const val RC_HANDLE_GMS = 9001
        private const val RC_HANDLE_CAMERA_PERM = 2

        private const val CAMERA_MODE = "camera_mode"
        private const val DOCUMENT_TYPE = "document_type"

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
    private var mode: ZippyActivity.CameraMode? = null
    private var documentType: DocumentType? = null
    private var cameraId = CameraSource.CAMERA_FACING_FRONT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onStart() {
        super.onStart()

        arguments = arguments
        mode = arguments?.getSerializable(CameraFragment.CAMERA_MODE) as? ZippyActivity.CameraMode
            ?: throw IllegalArgumentException("Mode was not passed to CameraFragment!")
        documentType = arguments?.getParcelable(CameraFragment.DOCUMENT_TYPE) as? DocumentType
            ?: throw IllegalArgumentException("Document type was not passed to CameraFragment!")

        if (mode == ZippyActivity.CameraMode.FACE) {
            showFaceFrame()
            cameraId = CameraSource.CAMERA_FACING_FRONT
            GraphicFaceTracker.mFaceDetectorListener = this
        } else if (mode == ZippyActivity.CameraMode.DOCUMENT_FRONT) {
            showDocumentFrontFrame(documentType!!)
            cameraId = CameraSource.CAMERA_FACING_BACK
            BarcodeTracker.mBarcodeDetectorListener = this
        } else if (mode == ZippyActivity.CameraMode.DOCUMENT_BACK) {
            showDocumentBackFrame(documentType!!)
            cameraId = CameraSource.CAMERA_FACING_BACK
            BarcodeTracker.mBarcodeDetectorListener = this
        }

        mPreview = cameraSourcePreview as CameraSourcePreview

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
    }

    private fun createCameraSource() {
        if (mode == ZippyActivity.CameraMode.FACE) {

            val faceDetector = FaceDetector.Builder(context!!)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build()
            faceDetector.setProcessor(
                MultiProcessor.Builder<Face>(GraphicFaceTrackerFactory())
                    .build())

            mCameraSource = CameraSource.Builder(context, faceDetector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(cameraId)
                .setRequestedFps(10.0f)
                .setAutoFocusEnabled(true)
                .build()

        } else {
            Log.e("DRIVERS_L", "IM IN CREATE CAMERA SOURCE ALRIGHT")

            val barcodeDetector = BarcodeDetector.Builder(context!!)
                .setBarcodeFormats(Barcode.DRIVER_LICENSE)
                .build()
            barcodeDetector.setProcessor(
                MultiProcessor.Builder<Barcode>(BarcodeTrackerFactory())
                    .build())

            mCameraSource = CameraSource.Builder(context, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(cameraId)
                .setRequestedFps(10.0f)
                .setAutoFocusEnabled(true)
                .build()
        }
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

        mCameraSource?.release()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: $requestCode")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source")
            createCameraSource()
            return
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.size +
                " Result code = " + if (grantResults.isNotEmpty()) grantResults[0] else "(empty)")

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
                mPreview!!.start(mCameraSource!!)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }
        }
    }

    private fun takePhoto() {
        val callback: CameraSource.PictureCallback = CameraSource.PictureCallback { data ->
            try {
                val loadedImage: Bitmap?

                val bitmapFactoryOpt = BitmapFactory.Options()
                bitmapFactoryOpt.inDensity = DisplayMetrics.DENSITY_DEFAULT
                bitmapFactoryOpt.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT
                bitmapFactoryOpt.inScaled = false

                loadedImage = BitmapFactory.decodeByteArray(data, 0, data.size, bitmapFactoryOpt)
                val rotatedImage: Bitmap?

                val exifInterface: ExifInterface?
                val byteArrayInputStream = ByteArrayInputStream(data)
                exifInterface = ExifInterface(byteArrayInputStream)
                var rotationDegrees = 0F
                val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
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
                val rotateMatrix = Matrix()
                rotateMatrix.postRotate(rotationDegrees)
                rotatedImage = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.width, loadedImage.height, rotateMatrix, false)
                (activity as ZippyActivity).onCaptureCompleted(rotatedImage)
            } catch (e: Exception) {
                e.printStackTrace()
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
            ZippyActivity.DocumentMode.PASSPORT.value -> {
                titleTv.text = getString(R.string.passport)
                descriptionTv.text = getString(R.string.position_passport_text)
            }
            ZippyActivity.DocumentMode.DRIVERS_LICENCE.value -> {
                titleTv.text = getString(R.string.driver_license_front)
                descriptionTv.text = getString(R.string.position_driver_license_front)
            }
            ZippyActivity.DocumentMode.ID_CARD.value -> {
                titleTv.text = getString(R.string.id_card_front)
                descriptionTv.text = getString(R.string.position_id_card_front)
            }
        }
    }

    private fun showDocumentBackFrame(documentType: DocumentType) {
        faceFrameLl.visibility = View.INVISIBLE
        documentFrontFrameLl.visibility = View.INVISIBLE
        documentBackFrameLl.visibility = View.VISIBLE

        when (documentType.value) {
            ZippyActivity.DocumentMode.DRIVERS_LICENCE.value -> {
                titleTv.text = getString(R.string.driver_license_back)
                descriptionTv.text = getString(R.string.position_driver_license_back)
            }
            ZippyActivity.DocumentMode.ID_CARD.value -> {
                titleTv.text = getString(R.string.id_card_back)
                descriptionTv.text = getString(R.string.position_id_card_back)
            }
        }
    }
}