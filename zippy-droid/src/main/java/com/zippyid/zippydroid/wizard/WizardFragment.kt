package com.zippyid.zippydroid.wizard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.android.volley.AuthFailureError
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.extension.observeLiveData
import com.zippyid.zippydroid.viewModel.*
import kotlinx.android.synthetic.main.fragment_wizard.*

class WizardFragment : Fragment() {
    lateinit var viewModelFactory: ZippyViewModelFactory
    private lateinit var viewModel: ZippyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wizard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModelFactory = ZippyViewModelFactory(context!!, (activity as ZippyActivity).getConfig())
        viewModel = ViewModelProviders.of((activity as ZippyActivity), viewModelFactory).get(ZippyViewModel::class.java)

        adjustViews()
        observeLiveData()
        viewModel.setZippyState(viewModel.state)
    }

    private fun observeLiveData() {
        viewLifecycleOwner.observeLiveData(viewModel.zippyStateLiveData) {
            processAccordingToState(it)
        }
        viewLifecycleOwner.observeLiveData(viewModel.verificationIdLiveData) {
            viewModel.pollJobStatus( null, it)
            Zippy.callback?.onSubmit()
        }
        viewLifecycleOwner.observeLiveData(viewModel.volleyErrorLiveData) {
            val message = if (it is AuthFailureError) {
                "Authorization error!"
            } else {
                "Unexpected error!"
            }
            (activity as ZippyActivity).sendErrorResult(message)
        }
        viewLifecycleOwner.observeLiveData(viewModel.cameraModeLiveData) { mode ->
            zippyBtn.setOnClickListener {
                if (mode != CameraMode.NONE) {
                    (activity as? ZippyActivity)?.toCameraFragment()
                } else {
                    zippyBtn.isEnabled = false
                    viewModel.sendImages()
                    sendingOkLabelTv.visibility = View.VISIBLE
                    zippyBtn.text = resources.getString(R.string.processing)
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
        viewLifecycleOwner.observeLiveData(viewModel.verificationStateAndZippyResult) { (verification, response) ->
            if (verification.state == "success" && viewModel.state == ZippyState.DONE) {
                (activity as ZippyActivity).sendSuccessfulResult(response)
            } else if (verification.state == "failed" && viewModel.state == ZippyState.DONE) {
                (activity as ZippyActivity).toErrorFragment()
            } else if (viewModel.state == ZippyState.DONE) {
                (activity as ZippyActivity).sendErrorResult("Unknown error")
            }
        }
        viewLifecycleOwner.observeLiveData(viewModel.shouldStopLiveData) {
            (activity as ZippyActivity).sendCancelledResult(it.second)
        }
    }

    private fun adjustViews() {
        if ((activity as ZippyActivity).getConfig().documentType.value == DocumentMode.PASSPORT.value) {
            documentBackLabelTv.visibility = View.GONE
            docBackOkLabelTv.text = ""
        }
    }

    private fun processAccordingToState(zippyState: ZippyState) {
        when(zippyState) {
            ZippyState.LOADING -> {
                viewModel.getToken()
            }
            ZippyState.READY -> {
                preparingOkLabelTv?.visibility = View.VISIBLE
                zippyBtn.text = resources.getString(R.string.take_selfie)
                viewModel.setCameraMode(CameraMode.FACE)
            }
            ZippyState.FACE_TAKEN -> {
                preparingOkLabelTv.visibility = View.VISIBLE
                faceOkLabelTv.visibility = View.VISIBLE
                zippyBtn.text = resources.getString(R.string.take_document_front)
                viewModel.setCameraMode(CameraMode.DOCUMENT_FRONT)
            }
            ZippyState.DOC_FRONT_TAKEN -> {
                preparingOkLabelTv.visibility = View.VISIBLE
                faceOkLabelTv.visibility = View.VISIBLE
                docFrontOkLabelTv.visibility = View.VISIBLE
                zippyBtn.text = resources.getString(R.string.take_document_back)
                viewModel.setCameraMode(CameraMode.DOCUMENT_BACK)
            }
            ZippyState.READY_TO_SEND -> {
                preparingOkLabelTv.visibility = View.VISIBLE
                faceOkLabelTv.visibility = View.VISIBLE
                docFrontOkLabelTv.visibility = View.VISIBLE
                docBackOkLabelTv.visibility = View.VISIBLE
                zippyBtn.text = resources.getString(R.string.send_info)
                viewModel.setCameraMode(CameraMode.NONE)
            }
            ZippyState.RETRY -> {
                viewModel.state = ZippyState.READY
                processAccordingToState(ZippyState.READY)
            }
            else -> {}
        }
    }
}