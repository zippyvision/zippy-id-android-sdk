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
        READY_TO_SEND
    }

    private var encodedFaceImage: String? = null
    private var encodedDocumentFrontImage: String? = null
    private var encodedDocumentBackImage: String? = null
    var state = ZippyState.LOADING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zippy)

        switchToIDVertification()
    }

    fun onWizardNextStep() {
        switchToCamera()
    }

    fun onCaptureCompleted(image: Image, imageOrientation: Int) {
        when (state) {
            ZippyState.READY -> {
                encodedFaceImage = image.toEncodedResizedPng(imageOrientation)
                state = ZippyState.FACE_TAKEN
                switchToWizard()
            }
            ZippyState.FACE_TAKEN -> {
                encodedDocumentFrontImage = image.toEncodedResizedPng(imageOrientation)
                state = ZippyState.DOC_FRONT_TAKEN
                switchToWizard()
            }
            ZippyState.DOC_FRONT_TAKEN -> {
                encodedDocumentBackImage = image.toEncodedResizedPng(imageOrientation)
                state = ZippyState.READY_TO_SEND
                switchToWizard()
            }
            else -> throw IllegalStateException("Unknown state after capture! Crashing...")
        }
    }

    fun sendImages(apiClient: ApiClient) {
        apiClient.sendImages(this, "id_card", encodedFaceImage!!, encodedDocumentFrontImage!!, encodedDocumentBackImage!!, "123456", object : AsyncResponse<SuccessResponse?> {
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
        finish()
    }

    fun switchToCamera() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, CameraFragment()).commit()
        Log.d("ZIPPY", "Switched to camera!")
    }

    fun switchToWizard() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, WizardFragment.newInstance(state)).commit()
        Log.d("ZIPPY", "Switched to wizard!")
    }

    fun switchToIDVertification() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, IDVertificationFragment()).commit()
        Log.d("ZIPPY", "Switched to ID vertification!")
    }
}
