package com.zippyid.zippydroid

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.VolleyError
import com.zippyid.zippydroid.camera.CameraFragment
import com.zippyid.zippydroid.extension.resize
import com.zippyid.zippydroid.extension.rotate
import com.zippyid.zippydroid.extension.*
import com.zippyid.zippydroid.network.ApiClient
import com.zippyid.zippydroid.network.AsyncResponse
import com.zippyid.zippydroid.network.model.DocumentType
import com.zippyid.zippydroid.network.model.SessionConfig
import com.zippyid.zippydroid.network.model.ZippyResponse
import com.zippyid.zippydroid.wizard.IDVertificationFragment
import com.zippyid.zippydroid.wizard.PhotoConfirmationFragment
import com.zippyid.zippydroid.wizard.WizardFragment

class ZippyActivity : AppCompatActivity() {
    companion object {
        const val ZIPPY_RESULT = "zippy_result"
        private const val TAG = "ZippyActivity"
    }

    enum class ZippyState {
        LOADING,
        READY,
        FACE_TAKEN,
        DOC_FRONT_TAKEN,
        DOC_BACK_TAKEN,
        READY_TO_SEND,
        DONE
    }

    enum class DocumentMode(val value: String) {
        PASSPORT("passport"),
        ID_CARD("id_card"),
        DRIVERS_LICENCE("drivers_licence")
    }

    enum class CameraMode {
        FACE,
        DOCUMENT_FRONT,
        DOCUMENT_BACK
    }

    private var encodedFaceImage: String? = null
    private var encodedDocumentFrontImage: String? = null
    private var encodedDocumentBackImage: String? = null

    var faceImage: Bitmap? = null
    var documentFrontImage: Bitmap? = null
    var documentBackImage: Bitmap? = null

    private var faceOrientation = 0
    private var documentFrontOrientation = 0
    private var documentBackOrientation = 0


    var state = ZippyState.LOADING
    private lateinit var documentType: DocumentType
    private lateinit var sessionConfiguration: SessionConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zippy)
        sessionConfiguration = intent.getParcelableExtra("SESSION_CONFIGURATION")
        switchToIDVerification()
    }

    fun onWizardNextStep(mode: CameraMode) {
        switchToCamera(mode)
    }

    fun onIDVerificationNextStep(documentType: DocumentType) {
        this.documentType = documentType
        switchToWizard(documentType)
    }

    fun onPhotoConfirmationIsReadableStep() {
        when (state) {
            ZippyState.READY -> {
                state = ZippyState.FACE_TAKEN
            }
            ZippyState.FACE_TAKEN -> {
                state = if (documentType.value == DocumentMode.PASSPORT.value) ZippyState.READY_TO_SEND else ZippyState.DOC_FRONT_TAKEN
            }
            ZippyState.DOC_FRONT_TAKEN -> {
                state = ZippyState.READY_TO_SEND
            }
        }
        switchToWizard(documentType)
    }

    fun onPhotoConfirmationTakeNewPhotoStep(mode: CameraMode) {
        switchToCamera(mode)
    }

    fun onCaptureCompleted(image: Image, imageOrientation: Int) {
        when (state) {
            ZippyState.READY -> {
                faceImage = image.toBitmap()
                faceOrientation = imageOrientation
                faceImage = faceImage!!.rotate(faceOrientation)
                faceImage?.let { switchToPhotoConfirmation(CameraMode.FACE) }
            }
            ZippyState.FACE_TAKEN -> {
                documentFrontImage = image.toBitmap()
                documentFrontOrientation = imageOrientation
                documentFrontImage = documentFrontImage!!.rotate(documentFrontOrientation)
                documentFrontImage?.let { switchToPhotoConfirmation(CameraMode.DOCUMENT_FRONT) }
            }
            ZippyState.DOC_FRONT_TAKEN -> {
                documentBackImage = image.toBitmap()
                documentBackOrientation = imageOrientation
                documentBackImage = documentBackImage!!.rotate(documentBackOrientation)
                documentBackImage?.let { switchToPhotoConfirmation(CameraMode.DOCUMENT_BACK) }
            }
            else -> throw IllegalStateException("Unknown state after capture! Crashing...")
        }
    }

    fun sendImages(apiClient: ApiClient, documentType: DocumentType) {
        if (documentType.value == null || faceImage == null || documentFrontImage == null) {
            Log.e("ERROR", "Missing data")
            state = ZippyState.LOADING
            switchToWizard(documentType)
            return
        }

        encodedFaceImage = faceImage!!.resize()!!.toEncodedPng()
        encodedDocumentFrontImage = documentFrontImage!!.resize()!!.toEncodedPng()
        encodedDocumentBackImage = documentBackImage?.resize()?.toEncodedPng()

        apiClient.sendImages(documentType.value, encodedFaceImage!!, encodedDocumentFrontImage!!, encodedDocumentBackImage, sessionConfiguration.customerId!!, object : AsyncResponse<Any?> {
            override fun onSuccess(response: Any?) {
                state = ZippyState.DONE
                pollJobStatus(apiClient, null)
            }

            override fun onError(error: VolleyError) {
                sendErrorResult(error.toString())
            }
        })
    }

    var count = 0
    fun pollJobStatus(apiClient: ApiClient, error: VolleyError?) {
        Log.i(TAG, "Trying to get status: $count")


        if (count == 10) {
            setResult(Activity.RESULT_CANCELED, null)
            val returnIntent = Intent()

            error?.let { returnIntent.putExtra(ZippyActivity.ZIPPY_RESULT, error.localizedMessage) } ?: run {
                returnIntent.putExtra(ZippyActivity.ZIPPY_RESULT, "Request timed out")
            }
            finish()
        }

        apiClient
            .getResult(sessionConfiguration.customerId!!, object : AsyncResponse<ZippyResponse?> {
                override fun onSuccess(response: ZippyResponse?) {
                    val returnIntent = Intent()
                    response?.let { returnIntent.putExtra(ZippyActivity.ZIPPY_RESULT, response) }

                    when {
                        !response?.state.isNullOrEmpty() && response?.state != "processing"-> {
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                        else -> {
                            Log.i(TAG, "Scheduling poll")
                            count += 1
                            Handler().postDelayed({
                                pollJobStatus(apiClient, null)
                            }, 2000)

                        }
                    }
                }

                override fun onError(error: VolleyError) {
                    Log.i(TAG, "Error")
                    count += 1
                    Handler().postDelayed({
                        pollJobStatus(apiClient, error)
                    }, 2000)
                }
            })
    }
    
    fun sendErrorResult(message: String) {
        val returnIntent = Intent()
        returnIntent.putExtra(ZippyActivity.ZIPPY_RESULT, message)
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    private fun switchToCamera(mode: CameraMode) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, CameraFragment.newInstance(mode, documentType)).commit()
        Log.d("ZIPPY", "Switched to camera!")
    }

    private fun switchToWizard(documentType: DocumentType) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, WizardFragment.newInstance(state, documentType)).commit()
        Log.d("ZIPPY", "Switched to wizard!")
    }

    private fun switchToIDVerification() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, IDVertificationFragment()).commit()
        Log.d("ZIPPY", "Switched to ID verification!")
    }

    private fun switchToPhotoConfirmation(mode: CameraMode) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, PhotoConfirmationFragment.newInstance(mode, documentType)).commit()
        Log.d("ZIPPY", "Switched to Photo confirmation!")
    }
}
