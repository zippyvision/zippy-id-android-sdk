package com.zippyid.zippydroid.wizard

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.network.ApiClient
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.network.model.DocumentType
import kotlinx.android.synthetic.main.fragment_photo_confirmation.*

class PhotoConfirmationFragment: Fragment()  {
    companion object {
        private const val CAMERA_MODE = "camera_mode"
        private const val DOCUMENT_TYPE = "document_type"

        fun newInstance(
            mode: ZippyActivity.CameraMode,
            documentType: DocumentType?
        ): PhotoConfirmationFragment {
            val bundle = Bundle()
            bundle.putSerializable(PhotoConfirmationFragment.CAMERA_MODE, mode)
            bundle.putParcelable(PhotoConfirmationFragment.DOCUMENT_TYPE, documentType)
            val fragment = PhotoConfirmationFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var mode: ZippyActivity.CameraMode
    private lateinit var documentType: DocumentType
    private lateinit var apiClient: ApiClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_photo_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiClient = ApiClient(Zippy.secret, Zippy.key, Zippy.host, context!!)

        mode = arguments?.getSerializable(PhotoConfirmationFragment.CAMERA_MODE) as? ZippyActivity.CameraMode
            ?: throw IllegalArgumentException("Mode was not passed to PhotoConfirmationFragment!")

        documentType = arguments?.getParcelable(PhotoConfirmationFragment.DOCUMENT_TYPE) as? DocumentType
            ?: throw IllegalArgumentException("Document type was not passed to CameraFragment!")

        adjustForMode()

        isReadableBtn.setOnClickListener {
            (activity as? ZippyActivity)?.onPhotoConfirmationIsReadableStep()
        }
        takePhotoBtn.setOnClickListener {
            (activity as? ZippyActivity)?.onPhotoConfirmationTakeNewPhotoStep(mode)
        }
    }

    fun adjustForMode() {
        var documentTypeLabel: String? = if (documentType.value == "id_card") documentType.label else documentType.label!!.toLowerCase()

        when(mode) {
            ZippyActivity.CameraMode.FACE -> {
                descriptionTv.text = "Make sure your face is recognizable, with no blur or glare"
                isReadableBtn.text = "My face is recognizable"
                photoIv.setImageBitmap((activity as ZippyActivity).faceImage)
            }
            ZippyActivity.CameraMode.DOCUMENT_FRONT -> {
                descriptionTv.text = "Make sure your ${documentTypeLabel} details are clear to read, with no blur or glare"
                isReadableBtn.text = "My ${documentTypeLabel} ir readable"
                photoIv.setImageBitmap((activity as ZippyActivity).documentFrontImage)

            }
            ZippyActivity.CameraMode.DOCUMENT_BACK -> {
                descriptionTv.text = "Make sure your ${documentTypeLabel} details are clear to read, with no blur or glare"
                isReadableBtn.text = "My ${documentTypeLabel} ir readable"
                photoIv.setImageBitmap((activity as ZippyActivity).documentBackImage)

            }
        }
    }

    fun rotateImage(bitmap: Bitmap): Bitmap {
        var matrix = Matrix()
        matrix.postRotate(-90F)
        var scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)
        return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
    }
}