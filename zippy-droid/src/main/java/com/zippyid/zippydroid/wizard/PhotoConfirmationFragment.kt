package com.zippyid.zippydroid.wizard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.databinding.FragmentPhotoConfirmationBinding
import com.zippyid.zippydroid.viewModel.CameraMode
import com.zippyid.zippydroid.viewModel.ZippyViewModel
import com.zippyid.zippydroid.viewModel.ZippyViewModelFactory

class PhotoConfirmationFragment: Fragment()  {
    lateinit var viewModelFactory: ZippyViewModelFactory
    private lateinit var viewModel: ZippyViewModel

    private lateinit var binding: FragmentPhotoConfirmationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPhotoConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModelFactory = ZippyViewModelFactory(context!!, (activity as ZippyActivity).getConfig())
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(ZippyViewModel::class.java)

        adjustForMode()

        binding.isReadableBtn.setOnClickListener {
            viewModel.switchState()
            findNavController().navigate(R.id.action_photoConfirmationFragment_to_wizardFragment)
        }
        binding.takePhotoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_photoConfirmationFragment_to_cameraFragment)
        }
    }

    private fun adjustForMode() {
        val documentTypeLabel: String? = if ((activity as ZippyActivity).getConfig().documentType.value == "id_card") (activity as ZippyActivity).getConfig().documentType.label else (activity as ZippyActivity).getConfig().documentType.label!!.toLowerCase()
        when(viewModel.mode) {
            CameraMode.FACE -> {
                binding.apply {
                    descriptionTv.text = resources.getString(R.string.check_face)
                    isReadableBtn.text = resources.getString(R.string.face_recognizable)
                    photoIv.setImageBitmap(viewModel.faceImage)
                }
            }
            CameraMode.DOCUMENT_FRONT -> {
                binding.apply {
                    descriptionTv.text = getString(R.string.check_document, documentTypeLabel)
                    isReadableBtn.text = getString(R.string.document_recognizable, documentTypeLabel)
                    photoIv.setImageBitmap(viewModel.documentFrontImage)
                }
            }
            CameraMode.DOCUMENT_BACK -> {
                binding.apply {
                    descriptionTv.text = getString(R.string.check_document, documentTypeLabel)
                    isReadableBtn.text = getString(R.string.document_recognizable, documentTypeLabel)
                    photoIv.setImageBitmap(viewModel.documentBackImage)
                }
            }
        }
    }
}