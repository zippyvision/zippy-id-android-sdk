package com.zippyid.zippydroid.wizard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.viewModel.CameraMode
import com.zippyid.zippydroid.viewModel.ZippyViewModel
import com.zippyid.zippydroid.viewModel.ZippyViewModelFactory
import kotlinx.android.synthetic.main.fragment_photo_confirmation.*

class PhotoConfirmationFragment: Fragment()  {
    lateinit var viewModelFactory: ZippyViewModelFactory
    private lateinit var viewModel: ZippyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_photo_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModelFactory = ZippyViewModelFactory(context!!, (activity as ZippyActivity).getConfig())
        viewModel = ViewModelProviders.of((activity as ZippyActivity), viewModelFactory).get(ZippyViewModel::class.java)

        adjustForMode()

        isReadableBtn.setOnClickListener {
            (activity as? ZippyActivity)?.onPhotoIsReadableStep(viewModel)
        }
        takePhotoBtn.setOnClickListener {
            (activity as? ZippyActivity)?.toCameraFragment()
        }
    }

    private fun adjustForMode() {
        val documentTypeLabel: String? = if ((activity as ZippyActivity).getConfig().documentType.value == "id_card") (activity as ZippyActivity).getConfig().documentType.label else (activity as ZippyActivity).getConfig().documentType.label!!.toLowerCase()
        when(viewModel.mode) {
            CameraMode.FACE -> {
                descriptionTv.text = resources.getString(R.string.check_face)
                isReadableBtn.text = resources.getString(R.string.face_recognizable)
                photoIv.setImageBitmap(viewModel.faceImage)
            }
            CameraMode.DOCUMENT_FRONT -> {
                descriptionTv.text = getString(R.string.check_document, documentTypeLabel)
                isReadableBtn.text = getString(R.string.document_recognizable, documentTypeLabel)
                photoIv.setImageBitmap(viewModel.documentFrontImage)
            }
            CameraMode.DOCUMENT_BACK -> {
                descriptionTv.text = getString(R.string.check_document, documentTypeLabel)
                isReadableBtn.text = getString(R.string.document_recognizable, documentTypeLabel)
                photoIv.setImageBitmap(viewModel.documentBackImage)
            }
        }
    }
}