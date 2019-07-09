package com.zippyid.zippydroid.wizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.network.ApiClient
import com.zippyid.zippydroid.network.model.DocumentType
import com.zippyid.zippydroid.network.model.ZippyVerification
import kotlinx.android.synthetic.main.fragment_error.*

class ErrorFragment: Fragment()  {
    companion object {
        private const val VERIFICATION_STATE = "verification_state"
        private const val DOCUMENT_TYPE = "document_type"

        fun newInstance(
            verification: ZippyVerification,
            documentType: DocumentType?
        ): ErrorFragment {
            val bundle = Bundle()
            bundle.putParcelable(VERIFICATION_STATE, verification)
            bundle.putParcelable(DOCUMENT_TYPE, documentType)
            val fragment = ErrorFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var documentType: DocumentType
    private lateinit var verification: ZippyVerification

    private lateinit var apiClient: ApiClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiClient = ApiClient(Zippy.token, Zippy.host, context!!)

        documentType = arguments?.getParcelable(DOCUMENT_TYPE) as? DocumentType
                ?: throw IllegalArgumentException("Document type was not passed to CameraFragment!")

        verification = arguments?.getParcelable(VERIFICATION_STATE) as? ZippyVerification
            ?: throw IllegalArgumentException("Verification state was not passed to CameraFragment!")

        val errorMessage = verification.error
        descriptionTv.text = if (errorMessage != null) "Reason: $errorMessage" else "Reason: unknown"

        verification.requestToken?.apply {
            apiClient.applyNewToken(this)
        }

        retryBtn.setOnClickListener {
            (activity as? ZippyActivity)?.onRetryStep(documentType)
        }
    }

}