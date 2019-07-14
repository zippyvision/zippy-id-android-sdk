package com.zippyid.zippydroid.wizard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.android.volley.AuthFailureError
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.databinding.FragmentWizardBinding
import com.zippyid.zippydroid.extension.observeLiveData
import com.zippyid.zippydroid.viewModel.*

class WizardFragment : Fragment() {
    lateinit var viewModelFactory: ZippyViewModelFactory
    private lateinit var viewModel: ZippyViewModel

    private lateinit var binding: FragmentWizardBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWizardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModelFactory = ZippyViewModelFactory(context!!, (activity as ZippyActivity).getConfig())
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(ZippyViewModel::class.java)

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
            binding.zippyBtn.setOnClickListener {
                if (mode != CameraMode.NONE) {
                    findNavController().navigate(R.id.action_wizardFragment_to_cameraFragment)
                } else {
                    binding.apply {
                        zippyBtn.isEnabled = false
                        viewModel.sendImages()
                        sendingOkLabelTv.visibility = View.VISIBLE
                        zippyBtn.text = resources.getString(R.string.processing)
                        progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
        viewLifecycleOwner.observeLiveData(viewModel.verificationStateAndZippyResult) { (verification, response) ->
            if (verification.state == "success" && viewModel.state == ZippyState.DONE) {
                (activity as ZippyActivity).sendSuccessfulResult(response)
            } else if (verification.state == "failed" && viewModel.state == ZippyState.DONE) {
                findNavController().navigate(R.id.action_wizardFragment_to_errorFragment)
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
            binding.documentBackLabelTv.visibility = View.GONE
            binding.docBackOkLabelTv.text = ""
        }
    }

    private fun processAccordingToState(zippyState: ZippyState) {
        when(zippyState) {
            ZippyState.LOADING -> {
                viewModel.getToken()
            }
            ZippyState.READY -> {
                binding.apply {
                    preparingOkLabelTv.visibility = View.VISIBLE
                    zippyBtn.text = resources.getString(R.string.take_selfie)
                }
                viewModel.setCameraMode(CameraMode.FACE)
            }
            ZippyState.FACE_TAKEN -> {
                binding.apply {
                    preparingOkLabelTv.visibility = View.VISIBLE
                    faceOkLabelTv.visibility = View.VISIBLE
                    zippyBtn.text = resources.getString(R.string.take_document_front)
                }
                viewModel.setCameraMode(CameraMode.DOCUMENT_FRONT)
            }
            ZippyState.DOC_FRONT_TAKEN -> {
                binding.apply {
                    preparingOkLabelTv.visibility = View.VISIBLE
                    faceOkLabelTv.visibility = View.VISIBLE
                    docFrontOkLabelTv.visibility = View.VISIBLE
                    zippyBtn.text = resources.getString(R.string.take_document_back)
                }
                viewModel.setCameraMode(CameraMode.DOCUMENT_BACK)
            }
            ZippyState.READY_TO_SEND -> {
                binding.apply {
                    preparingOkLabelTv.visibility = View.VISIBLE
                    faceOkLabelTv.visibility = View.VISIBLE
                    docFrontOkLabelTv.visibility = View.VISIBLE
                    docBackOkLabelTv.visibility = View.VISIBLE
                    zippyBtn.text = resources.getString(R.string.send_info)
                }
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