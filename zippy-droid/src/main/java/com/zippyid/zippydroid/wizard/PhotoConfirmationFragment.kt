package com.zippyid.zippydroid.wizard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.support.v4.app.Fragment;
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.network.ApiClient
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.ZippyActivity
import kotlinx.android.synthetic.main.fragment_photo_confirmation.*

class PhotoConfirmationFragment: Fragment()  {
    companion object {
        private const val CAMERA_MODE = "camera_mode"
        private const val BITMAP_IMAGE_BYTE_ARRAY = "bitmap_image_byte_array"

        fun newInstance(
            mode: ZippyActivity.CameraMode,
            imageByteArray: ByteArray
        ): PhotoConfirmationFragment {
            val bundle = Bundle()
            bundle.putSerializable(PhotoConfirmationFragment.CAMERA_MODE, mode)
            bundle.putByteArray(PhotoConfirmationFragment.BITMAP_IMAGE_BYTE_ARRAY, imageByteArray)
            val fragment = PhotoConfirmationFragment()
            fragment.arguments = bundle
            return fragment
        }
    }


    private lateinit var imageByteArray: ByteArray
    private lateinit var mode: ZippyActivity.CameraMode
    private lateinit var apiClient: ApiClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_photo_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiClient = ApiClient(Zippy.secret, Zippy.key, Zippy.host, context!!)

        mode = arguments?.getSerializable(PhotoConfirmationFragment.CAMERA_MODE) as? ZippyActivity.CameraMode
            ?: throw IllegalArgumentException("Mode was not passed to PhotoConfirmationFragment!")

        adjustForMode()

        imageByteArray = arguments?.getByteArray(PhotoConfirmationFragment.BITMAP_IMAGE_BYTE_ARRAY)
            ?: throw IllegalArgumentException("Bitmap image byte array was not passed to PhotoConfirmationFragment!")

        var bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)

        if (mode == ZippyActivity.CameraMode.FACE) {
            photoIv.setImageBitmap(bitmap)
        } else {
            bitmap = rotateImage(bitmap)
            photoIv.setImageBitmap(bitmap)
        }

        isReadableBtn.setOnClickListener {
            (activity as? ZippyActivity)?.onPhotoConfirmationIsReadableStep()
        }
        takePhotoBtn.setOnClickListener {
            (activity as? ZippyActivity)?.onPhotoConfirmationTakeNewPhotoStep(mode)
        }
    }

    fun adjustForMode() {
        when(mode) {
            ZippyActivity.CameraMode.FACE -> {
                descriptionTv.text = "Make sure your face is recognizable, with no blur or glare"
                isReadableBtn.text = "My face is recognizable"
            }
            ZippyActivity.CameraMode.DOCUMENT_FRONT, ZippyActivity.CameraMode.DOCUMENT_BACK -> {
                descriptionTv.text = "Make sure your license details are clear to read, with no blur or glare"
                isReadableBtn.text = "My license ir readable"
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