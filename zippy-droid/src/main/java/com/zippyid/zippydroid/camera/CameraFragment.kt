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

import com.zippyid.zippydroid.camera.helpers.CameraSourcePreview
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import java.io.IOException
import java.lang.Exception
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.network.model.DocumentType
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.camera.helpers.GraphicFaceTracker
import com.zippyid.zippydroid.camera.helpers.GraphicFaceTrackerFactory
import com.zippyid.zippydroid.databinding.FragmentCameraBinding
import com.zippyid.zippydroid.viewModel.CameraMode
import com.zippyid.zippydroid.viewModel.DocumentMode
import com.zippyid.zippydroid.viewModel.ZippyViewModel
import com.zippyid.zippydroid.viewModel.ZippyViewModelFactory
import java.io.ByteArrayInputStream

class CameraFragment : Fragment(), GraphicFaceTracker.FaceDetectorListener {
    override fun onFaceDetected(face: Face) {
        takePhoto()
    }

    companion object {
        private const val TAG = "FaceTracker"
        private const val RC_HANDLE_GMS = 9001
        private const val RC_HANDLE_CAMERA_PERM = 2
    }

    private lateinit var viewModelFactory: ZippyViewModelFactory
    private lateinit var viewModel: ZippyViewModel

    private lateinit var binding: FragmentCameraBinding

    private var mCameraSource: CameraSource? = null
    private var mPreview: CameraSourcePreview? = null
    private var cameraId = CameraSource.CAMERA_FACING_FRONT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        viewModelFactory = ZippyViewModelFactory(context!!, (activity as ZippyActivity).getConfig())
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(ZippyViewModel::class.java)

        when (viewModel.mode) {
            CameraMode.FACE -> {
                showFaceFrame()
                cameraId = CameraSource.CAMERA_FACING_FRONT
                GraphicFaceTracker.mFaceDetectorListener = this
            }
            CameraMode.DOCUMENT_FRONT -> {
                showDocumentFrontFrame((activity as ZippyActivity).getConfig().documentType)
                cameraId = CameraSource.CAMERA_FACING_BACK
            }
            CameraMode.DOCUMENT_BACK -> {
                showDocumentBackFrame((activity as ZippyActivity).getConfig().documentType)
                cameraId = CameraSource.CAMERA_FACING_BACK
            }
            else -> {}
        }

        mPreview = binding.cameraSourcePreview

        val rc = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource()
            binding.takePictureBtn.setOnClickListener { takePhoto() }
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
        val detector = FaceDetector.Builder(context!!)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setMode(FaceDetector.ACCURATE_MODE)
            .build()
        detector.setProcessor(
            MultiProcessor.Builder(GraphicFaceTrackerFactory())
                .build())

        mCameraSource = CameraSource.Builder(context, detector)
            .setRequestedPreviewSize(640, 480)
            .setFacing(cameraId)
            .setRequestedFps(10.0f)
            .setAutoFocusEnabled(true)
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

        val listener = DialogInterface.OnClickListener { _, _ -> onStop() }

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
                when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
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

                viewModel.addImage(rotatedImage)
                findNavController().navigate(R.id.action_cameraFragment_to_photoConfirmationFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mCameraSource!!.takePicture(null, callback)
    }

    private fun showFaceFrame() {
        binding.apply {
            faceFrameLl.visibility = View.VISIBLE
            documentFrontFrameLl.visibility = View.INVISIBLE
            documentBackFrameLl.visibility = View.INVISIBLE

            titleTv.text = ""
            descriptionTv.text = ""
        }
    }

    private fun showDocumentFrontFrame(documentType: DocumentType) {
        binding.apply {
            faceFrameLl.visibility = View.INVISIBLE
            documentFrontFrameLl.visibility = View.VISIBLE
            documentBackFrameLl.visibility = View.INVISIBLE

            when (documentType.value) {
                DocumentMode.PASSPORT.value -> {
                    titleTv.text = getString(R.string.passport)
                    descriptionTv.text = getString(R.string.position_passport_text)
                }
                DocumentMode.DRIVERS_LICENCE.value -> {
                    titleTv.text = getString(R.string.driver_license_front)
                    descriptionTv.text = getString(R.string.position_driver_license_front)
                }
                DocumentMode.ID_CARD.value -> {
                    titleTv.text = getString(R.string.id_card_front)
                    descriptionTv.text = getString(R.string.position_id_card_front)
                }
            }
        }
    }

    private fun showDocumentBackFrame(documentType: DocumentType) {
        binding.apply {
            faceFrameLl.visibility = View.INVISIBLE
            documentFrontFrameLl.visibility = View.INVISIBLE
            documentBackFrameLl.visibility = View.VISIBLE

            when (documentType.value) {
                DocumentMode.DRIVERS_LICENCE.value -> {
                    titleTv.text = getString(R.string.driver_license_back)
                    descriptionTv.text = getString(R.string.position_driver_license_back)
                }
                DocumentMode.ID_CARD.value -> {
                    titleTv.text = getString(R.string.id_card_back)
                    descriptionTv.text = getString(R.string.position_id_card_back)
                }
            }
        }
    }
}