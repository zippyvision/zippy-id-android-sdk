package com.zippyid.zippydroid

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.VolleyError
import com.zippyid.zippydroid.camera.CameraFragment
import com.zippyid.zippydroid.extension.toEncodedResizedPng
import com.zippyid.zippydroid.network.ApiClient
import com.zippyid.zippydroid.network.AsyncResponse
import com.zippyid.zippydroid.network.model.DocumentType
import com.zippyid.zippydroid.network.model.SuccessResponse
import com.zippyid.zippydroid.wizard.IDVertificationFragment
import com.zippyid.zippydroid.wizard.WizardFragment

class ZippyActivity : AppCompatActivity() {
    companion object {
        const val ZIPPY_RESULT = "zippy_result"
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

    private var encodedFaceImage: String? = null
    private var encodedDocumentFrontImage: String? = null
    private var encodedDocumentBackImage: String? = null
    var state = ZippyState.LOADING
    lateinit var documentType: DocumentType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zippy)

        switchToIDVertification()
    }

    fun onWizardNextStep(mode: String) {
        switchToCamera(mode)
    }

    fun onIDVertificationNextStep(documentType: DocumentType) {
        this.documentType = documentType
        switchToWizard(documentType)
    }

    fun onCaptureCompleted(image: Image, imageOrientation: Int) {
        when (state) {
            ZippyState.READY -> {
                encodedFaceImage = image.toEncodedResizedPng(imageOrientation)
                state = ZippyState.FACE_TAKEN
                switchToWizard(documentType)
            }
            ZippyState.FACE_TAKEN -> {
                encodedDocumentFrontImage = image.toEncodedResizedPng(imageOrientation)
                if (documentType.value == "passport") {
                    state = ZippyState.READY_TO_SEND
                } else {
                    state = ZippyState.DOC_FRONT_TAKEN
                }
                switchToWizard(documentType)
            }
            ZippyState.DOC_FRONT_TAKEN -> {
                encodedDocumentBackImage = image.toEncodedResizedPng(imageOrientation)
                state = ZippyState.READY_TO_SEND
                switchToWizard(documentType)
            }
            else -> throw IllegalStateException("Unknown state after capture! Crashing...")
        }
    }

    fun sendImages(apiClient: ApiClient) {
        apiClient.sendImages(this, "id_card", encodedFaceImage!!, encodedDocumentFrontImage!!, encodedDocumentBackImage, "123456", object : AsyncResponse<SuccessResponse?> {
            override fun onSuccess(response: SuccessResponse?) {
                //TODO add proper response handling
                finishAndSendResult(response?.state ?: "Couldn't handle response")
            }

            override fun onError(error: VolleyError) {
                finishAndSendResult("Sent images, aaaand... Error!")
            }
        })
    }

    fun finishAndSendResult(message: String) {
        val returnIntent = Intent()
        returnIntent.putExtra(ZippyActivity.ZIPPY_RESULT, message)
        setResult(Activity.RESULT_OK, returnIntent)
        state = ZippyState.DONE
        switchToWizard(documentType)
    }

    private fun switchToCamera(mode: String) {
        val cameraFragment = CameraFragment()
        val arguments = Bundle()
        arguments.putString("mode", mode)
        cameraFragment.setArguments(arguments)

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, cameraFragment).commit()
        Log.d("ZIPPY", "Switched to camera!")
    }

    private fun switchToWizard(documentType: DocumentType) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, WizardFragment.newInstance(state, documentType)).commit()
        Log.d("ZIPPY", "Switched to wizard!")
    }

    private fun switchToIDVertification() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, IDVertificationFragment()).commit()
        Log.d("ZIPPY", "Switched to ID vertification!")
    }
}
