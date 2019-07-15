package com.zippyid.zippydroid.viewModel

import android.graphics.Bitmap
import com.zippyid.zippydroid.extension.*
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.AuthFailureError
import com.android.volley.VolleyError
import com.zippyid.zippydroid.network.ApiClient
import com.zippyid.zippydroid.network.AsyncResponse
import com.zippyid.zippydroid.network.model.*

class ZippyViewModel(private val apiClient: ApiClient, private val configuration: SessionConfig): ViewModel() {
    companion object {
        private const val TAG = "ZippyViewModel"
    }

    private var _zippyState = MutableLiveData<ZippyState>()
    private var _volleyError = MutableLiveData<VolleyError>()
    private var _verificationId = MutableLiveData<String>()
    private var _cameraMode = MutableLiveData<CameraMode>()
    private var _countries = MutableLiveData<List<Country>>()
    private var _verificationStateAndZippyResult = MutableLiveData<Pair<ZippyVerification, ZippyResponse>>()
    private var _sendSuccessfulResult = MutableLiveData<ZippyResponse>()
    private var _sendCanceledResult = MutableLiveData<VolleyError?>()
    private var _sendErrorResult = MutableLiveData<String>()

    private var count = 0

    init {
        Log.d(TAG, "injecting ZippyViewModel")
    }

    val zippyStateLiveData: LiveData<ZippyState> = _zippyState
    val volleyErrorLiveData: LiveData<VolleyError> = _volleyError
    val verificationIdLiveData: LiveData<String> = _verificationId
    val cameraModeLiveData: LiveData<CameraMode> = _cameraMode
    val countriesLiveData: LiveData<List<Country>> = _countries
    val verificationStateAndZippyResult: LiveData<Pair<ZippyVerification, ZippyResponse>> = _verificationStateAndZippyResult
    val sendSuccessfulResultLiveData: LiveData<ZippyResponse> = _sendSuccessfulResult
    val sendCanceledResultLiveData: LiveData<VolleyError?> = _sendCanceledResult
    val sendErrorResultLiveData: LiveData<String> = _sendErrorResult

    var state: ZippyState = ZippyState.LOADING
    var mode: CameraMode? = null

    var faceImage: Bitmap? = null
    var documentFrontImage: Bitmap? = null
    var documentBackImage: Bitmap? = null

    fun setCameraMode(cameraMode: CameraMode) {
        mode = cameraMode
        _cameraMode.value = cameraMode
    }

    fun setZippyState(zippyState: ZippyState) {
        state = zippyState
        _zippyState.value = zippyState
    }

    fun getToken() {
        apiClient.getToken(object : AsyncResponse<String> {
            override fun onSuccess(response: String) {
                setZippyState(ZippyState.READY)
            }
            override fun onError(error: VolleyError) {
                val message = if (error is AuthFailureError) {
                    "Authorization error!"
                } else {
                    "Unexpected error!"
                }
                _sendErrorResult.value = message
            }
        })
    }

    fun getCountries() {
        apiClient.getCountries(object : AsyncResponse<List<Country>> {
            override fun onSuccess(response: List<Country>) {
                _countries.value = response
            }
            override fun onError(error: VolleyError) {
                _volleyError.value = error
            }
        })
    }

    fun applyNewToken(newToken: String) {
        apiClient.applyNewToken(newToken)
    }

    fun sendImages() {
        if (configuration.documentType.value == null || faceImage == null || documentFrontImage == null) {
            Log.e("ERROR", "Missing data")
            setZippyState(ZippyState.LOADING)
            return
        }

        val encodedFaceImage = faceImage!!.resize()!!.toEncodedPng()
        val encodedDocumentFrontImage = documentFrontImage!!.resize()!!.toEncodedPng()
        val encodedDocumentBackImage = documentBackImage?.resize()?.toEncodedPng()

        apiClient.sendImages(configuration.documentType.value!!, encodedFaceImage, encodedDocumentFrontImage, encodedDocumentBackImage, configuration.customerId, object : AsyncResponse<String?> {
            override fun onSuccess(response: String?) {
                setZippyState(ZippyState.DONE)
                _verificationId.value = response
            }
            override fun onError(error: VolleyError) {
                val message = if (error is AuthFailureError) {
                    "Authorization error!"
                } else {
                    "Unexpected error!"
                }
                _sendErrorResult.value = message
            }
        })
    }

    fun pollJobStatus(error: VolleyError?, verificationId: String) {
        Log.i(TAG, "Trying to get status: $count")

        if (count == 10) {
            _sendCanceledResult.value = error
        }

        apiClient
            .getResult(configuration.customerId, object : AsyncResponse<ZippyResponse?> {
                override fun onSuccess(response: ZippyResponse?) {
                    when {
                        !response?.state.isNullOrEmpty() && response?.state != "processing" -> {
                            getVerificationInformation(verificationId, response!!)
                        }
                        else -> {
                            Log.i(TAG, "Scheduling poll")
                            count += 1
                            Handler().postDelayed({
                                pollJobStatus(error, verificationId)
                            }, 2000)
                        }
                    }
                }
                override fun onError(error: VolleyError) {
                    Log.i(TAG, "Error")
                    count += 1
                    Handler().postDelayed({
                        pollJobStatus(error, verificationId)
                    }, 2000)
                }
            })
    }

    fun getVerificationInformation(verificationId: String, zippyResponse: ZippyResponse) {
        apiClient
            .checkVerificationStatus(verificationId, object : AsyncResponse<ZippyVerification?> {
                override fun onSuccess(response: ZippyVerification?) {
                    response?.apply {
                        _verificationStateAndZippyResult.value = Pair(response, zippyResponse)
                        processVerification(response, zippyResponse)
                    }
                    response?.requestToken?.apply {
                        applyNewToken(this)
                    }
                }
                override fun onError(error: VolleyError) {
                    _volleyError.value = error
                }
            })
    }

    fun processVerification(verification: ZippyVerification, zippyResponse: ZippyResponse) {
        if (verification.state == "success" && state == ZippyState.DONE) {
            _sendSuccessfulResult.value = zippyResponse
        } else if (verification.state != "failed" && state == ZippyState.DONE) {
            _sendErrorResult.value = "Unknown error"
        }
    }

    fun addImage(image: Bitmap) {
        when (state) {
            ZippyState.READY -> {
                faceImage = image
                setCameraMode(CameraMode.FACE)
            }
            ZippyState.FACE_TAKEN -> {
                documentFrontImage = image
                setCameraMode(CameraMode.DOCUMENT_FRONT)
            }
            ZippyState.DOC_FRONT_TAKEN -> {
                documentBackImage = image
                setCameraMode(CameraMode.DOCUMENT_BACK)
            }
            else -> throw IllegalStateException("Unknown state after capture! Crashing...")
        }
    }

    fun switchState() {
        when (state) {
            ZippyState.READY -> {
                setZippyState(ZippyState.FACE_TAKEN)
            }
            ZippyState.FACE_TAKEN -> {
                setZippyState(
                    if (configuration.documentType.value == DocumentMode.PASSPORT.value) {
                        ZippyState.READY_TO_SEND
                    } else {
                        ZippyState.DOC_FRONT_TAKEN
                    }
                )
            }
            ZippyState.DOC_FRONT_TAKEN -> {
                setZippyState(ZippyState.READY_TO_SEND)
            }
            else -> {}
        }
    }
}

enum class ZippyState {
    LOADING,
    READY,
    FACE_TAKEN,
    DOC_FRONT_TAKEN,
    DOC_BACK_TAKEN,
    READY_TO_SEND,
    DONE,
    RETRY
}

enum class DocumentMode(val value: String) {
    PASSPORT("passport"),
    ID_CARD("id_card"),
    DRIVERS_LICENCE("drivers_licence")
}

enum class CameraMode {
    FACE,
    DOCUMENT_FRONT,
    DOCUMENT_BACK,
    NONE
}