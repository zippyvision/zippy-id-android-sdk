package com.zippyid.zippydroid.camera

import android.Manifest
import android.util.Size
import android.graphics.Matrix
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Rational
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.ZippyActivity
import kotlinx.android.synthetic.main.fragment_camera.*
import com.zippyid.zippydroid.camera.helpers.LuminosityAnalyzer
import com.zippyid.zippydroid.extension.toBitmap
import com.zippyid.zippydroid.extension.toEncodedPng
import com.zippyid.zippydroid.network.model.DocumentType

class CameraFragment : Fragment(), LifecycleOwner {
    companion object {
        private const val TAG = "FaceTracker"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        private const val CAMERA_MODE = "camera_mode"
//        private const val DOCUMENT_TYPE = "document_type"

        fun newInstance(
            mode: ZippyActivity.CameraMode
//            documentType: DocumentType?
        ): CameraFragment {
            val bundle = Bundle()
            bundle.putSerializable(CAMERA_MODE, mode)
//            bundle.putParcelable(DOCUMENT_TYPE, documentType)
            val fragment = CameraFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var cameraMode: ZippyActivity.CameraMode
//    private lateinit var documentType: DocumentType

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments = arguments
        cameraMode = arguments?.getSerializable(CameraFragment.CAMERA_MODE) as? ZippyActivity.CameraMode
            ?: throw IllegalArgumentException("Mode was not passed to CameraFragment!")
//        val documentType = arguments?.getParcelable(CameraFragment.DOCUMENT_TYPE) as? DocumentType
//            ?: throw IllegalArgumentException("Document type was not passed to CameraFragment!")

        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this.requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private fun startCamera() {
//        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
//        val aspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)
//        val rotation = viewFinder.display.rotation
//        val resolution = Size(metrics.widthPixels, metrics.heightPixels)

        var facing = if (cameraMode == ZippyActivity.CameraMode.FACE) CameraX.LensFacing.FRONT else CameraX.LensFacing.BACK

        val aspectRatio = Rational(viewFinder.width, viewFinder.height)
        val rotation = viewFinder.display.rotation
        val resolution = Size(viewFinder.width, viewFinder.height)

        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(aspectRatio) // kkas nav bet būtu jābūt
            setTargetRotation(rotation)
            setTargetResolution(resolution)
            setLensFacing(facing)
        }.build()

        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener {
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setTargetAspectRatio(aspectRatio) // kkas nav bet būtu jābūt
                setTargetRotation(rotation)
                setTargetResolution(resolution)
                setLensFacing(facing)
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)
        takePictureBtn.setOnClickListener {
            imageCapture.takePicture(object : ImageCapture.OnImageCapturedListener() {
                override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
                    val msg = "Photo capture failed $message"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.e("CameraXApp", msg)
                }

                override fun onCaptureSuccess(image: ImageProxy, rotationDegrees: Int) {
                    super.onCaptureSuccess(image, rotationDegrees)
                    val msg = "Photo capture succeeded."
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.d("CameraXApp", msg)
                    image.image.let {
                        Log.d("CameraXApp", msg)
                        (activity as ZippyActivity).onCaptureCompleted(it!!.toBitmap()!!) // error

                    }
                }
            })
        }

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            val analyzerThread = HandlerThread(
                "LuminosityAnalysis").apply { start() }
            setLensFacing(facing)
            setCallbackHandler(Handler(analyzerThread.looper))
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            analyzer = LuminosityAnalyzer()
        }

        CameraX.bindToLifecycle(this, preview, imageCapture, analyzerUseCase)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        viewFinder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(context, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
//                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
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