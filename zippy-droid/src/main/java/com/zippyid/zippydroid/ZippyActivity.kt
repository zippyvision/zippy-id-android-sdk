package com.zippyid.zippydroid

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.android.volley.VolleyError
import com.zippyid.zippydroid.camera.CameraFragment
import com.zippyid.zippydroid.network.model.SessionConfig
import com.zippyid.zippydroid.network.model.ZippyResponse
import com.zippyid.zippydroid.viewModel.*
import com.zippyid.zippydroid.wizard.ErrorFragment
import com.zippyid.zippydroid.wizard.IDVerificationFragment
import com.zippyid.zippydroid.wizard.PhotoConfirmationFragment
import com.zippyid.zippydroid.wizard.WizardFragment

class ZippyActivity : AppCompatActivity() {
    companion object {
        const val ZIPPY_RESULT = "zippy_result"
        private const val TAG = "ZippyActivity"
    }

    private var sessionConfiguration: SessionConfig? = null

    fun getConfig(): SessionConfig {
        return sessionConfiguration ?: intent.getParcelableExtra("SESSION_CONFIGURATION")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zippy)
        switchToIDVerification()
    }

    fun sendSuccessfulResult(response: ZippyResponse) {
        val returnIntent = Intent()
        returnIntent.putExtra(ZIPPY_RESULT, response)
        Zippy.callback?.onFinished()
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    fun sendErrorResult(message: String) {
        val returnIntent = Intent()
        returnIntent.putExtra(ZIPPY_RESULT, message)
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    fun sendCancelledResult(error: VolleyError?) {
        setResult(Activity.RESULT_CANCELED, null)
        val returnIntent = Intent()
        error?.let { returnIntent.putExtra(ZIPPY_RESULT, error.localizedMessage) } ?: run {
            returnIntent.putExtra(ZIPPY_RESULT, "Request timed out")
        }
        finish()
    }

    fun onPhotoIsReadableStep(viewModel: ZippyViewModel) {
        when (viewModel.state) {
            ZippyState.READY -> {
                viewModel.setZippyState(ZippyState.FACE_TAKEN)
            }
            ZippyState.FACE_TAKEN -> {
                if (getConfig().documentType.value == DocumentMode.PASSPORT.value) {
                    viewModel.setZippyState(ZippyState.READY_TO_SEND)
                } else {
                    viewModel.setZippyState(ZippyState.DOC_FRONT_TAKEN)
                }
            }
            ZippyState.DOC_FRONT_TAKEN -> {
                viewModel.setZippyState(ZippyState.READY_TO_SEND)
            }
            else -> {}
        }
        switchToWizard()
    }

    fun onCaptureCompleted(image: Bitmap, viewModel: ZippyViewModel) {
        when (viewModel.state) {
            ZippyState.READY -> {
                viewModel.faceImage = image
                viewModel.setCameraMode(CameraMode.FACE)
                viewModel.faceImage?.let {
                    switchToPhotoConfirmation()
                }
            }
            ZippyState.FACE_TAKEN -> {
                viewModel.documentFrontImage = image
                viewModel.setCameraMode(CameraMode.DOCUMENT_FRONT)
                viewModel.documentFrontImage?.let {
                    switchToPhotoConfirmation()
                }
            }
            ZippyState.DOC_FRONT_TAKEN -> {
                viewModel.documentBackImage = image
                viewModel.setCameraMode(CameraMode.DOCUMENT_BACK)
                viewModel.documentBackImage?.let {
                    switchToPhotoConfirmation()
                }
            }
            else -> throw IllegalStateException("Unknown state after capture! Crashing...")
        }
    }

    fun toIDVerificationFragment() {
        switchToIDVerification()
    }

    fun toCameraFragment() {
        switchToCamera()
    }

    fun toWizardFragment() {
        switchToWizard()
    }

    fun toErrorFragment() {
        switchToErrorFragment()
    }

    private fun switchToCamera() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, CameraFragment()).commit()
        Log.d(TAG, "Switched to camera!")
    }

    private fun switchToWizard() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, WizardFragment()).commit()
        Log.d(TAG, "Switched to wizard!")
    }

    private fun switchToIDVerification() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, IDVerificationFragment()).commit()
        Log.d(TAG, "Switched to ID verification!")
    }

    private fun switchToPhotoConfirmation() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, PhotoConfirmationFragment()).commit()
        Log.d(TAG, "Switched to Photo confirmation!")
    }

    private fun switchToErrorFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_fl, ErrorFragment()).commit()
        Log.d(TAG, "Switched to Error fragment!")
    }
}