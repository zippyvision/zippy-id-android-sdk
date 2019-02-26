package com.zippyid.zippydroid.camera.face_tracker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.zippyid.zippydroid.camera.face_tracker.CameraSourcePreview
import com.zippyid.zippydroid.camera.face_tracker.GraphicOverlay
import com.zippyid.zippydroid.R

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.camera.CameraFragment
import com.zippyid.zippydroid.network.model.DocumentType
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.android.synthetic.main.fragment_face_camera.*

import java.io.IOException

class CameraActivityFragment : Fragment() {
    companion object {

        private val TAG = "FaceTracker"
        private val RC_HANDLE_GMS = 9001
        private val RC_HANDLE_CAMERA_PERM = 2

        private const val STATE_PREVIEW = 0
        private const val STATE_WAITING_LOCK = 1
        private const val STATE_WAITING_PRECAPTURE = 2
        private const val STATE_WAITING_NON_PRECAPTURE = 3
        private const val STATE_PICTURE_TAKEN = 4

        private const val MAX_PREVIEW_WIDTH = 1920
        private const val MAX_PREVIEW_HEIGHT = 1080


        private const val CAMERA_MODE = "camera_mode"
        private const val DOCUMENT_TYPE = "document_type"

        fun newInstance(
            mode: ZippyActivity.CameraMode,
            documentType: DocumentType?
        ): CameraActivityFragment {
            val bundle = Bundle()
            bundle.putSerializable(CameraActivityFragment.CAMERA_MODE, mode)
            bundle.putParcelable(CameraActivityFragment.DOCUMENT_TYPE, documentType)
            val fragment = CameraActivityFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private var mCameraSource: CameraSource? = null
    private var mPreview: CameraSourcePreview? = null
    private var mGraphicOverlay: GraphicOverlay? = null

    private var state = CameraActivityFragment.STATE_PREVIEW
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var captureSession: CameraCaptureSession? = null

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_face_camera, container, false)
    }

    override fun onStart() {
        super.onStart()
        mPreview = preview as CameraSourcePreview
        mGraphicOverlay = faceOverlay as GraphicOverlay
        val rc = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource()
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

        val context = context!!
        val detector = FaceDetector.Builder(context)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setMode(FaceDetector.ACCURATE_MODE)
            .build()
        detector.setProcessor(
            MultiProcessor.Builder<Face>(mGraphicOverlay?.let { GraphicFaceTrackerFactory(it) })
                .build())

        if (!detector.isOperational) {
            Log.w(TAG, "Face detector dependencies are not yet available.")
        }
        mCameraSource = CameraSource.Builder(context, detector)
            .setRequestedPreviewSize(640, 480)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
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

    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (state) {
                CameraActivityFragment.STATE_PREVIEW -> {
                    val faces: Array<android.hardware.camera2.params.Face>? = result.get(CaptureResult.STATISTICS_FACES)
                    if (faces?.size == 1) {
                        takePicture()
                    }
                }
                CameraActivityFragment.STATE_WAITING_LOCK -> {
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null || afState == 0) {
                        state = CameraActivityFragment.STATE_PICTURE_TAKEN
                        captureStillPicture()
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            state = CameraActivityFragment.STATE_PICTURE_TAKEN
                            captureStillPicture()
                        } else {
                            runPrecaptureSequence()
                        }
                    }
                }
                CameraActivityFragment.STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED
                    ) {
                        state = CameraActivityFragment.STATE_WAITING_NON_PRECAPTURE
                    }
                }
                CameraActivityFragment.STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = CameraActivityFragment.STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            process(result)
        }
    }

    private fun takePicture() {
        activity?.runOnUiThread{
            if (cameraProgressBar != null && whiteBackground != null) {
                whiteBackground.visibility = View.VISIBLE
                cameraProgressBar.visibility = View.VISIBLE
            }
        }
        lockFocus()
    }

    private fun lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            previewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START
            )
            // Tell #mCaptureCallback to wait for the lock.
            state = CameraActivityFragment.STATE_WAITING_LOCK
            captureSession!!.capture(
                previewRequestBuilder!!.build(), mCaptureCallback,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }
}
