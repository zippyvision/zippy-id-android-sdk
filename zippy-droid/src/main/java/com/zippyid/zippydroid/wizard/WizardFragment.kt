package com.zippyid.zippydroid.wizard

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.volley.VolleyError
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.network.ApiClient
import com.zippyid.zippydroid.network.AsyncResponse
import com.zippyid.zippydroid.network.model.DocumentType
import kotlinx.android.synthetic.main.fragment_wizard.*

class WizardFragment : Fragment() {
    companion object {
        private const val ZIPPY_STATE = "zippy_state"
        private const val DOCUMENT_TYPE = "document_type"

        fun newInstance(
            state: ZippyActivity.ZippyState,
            documentType: DocumentType?
        ): WizardFragment {
            val bundle = Bundle()
            bundle.putSerializable(ZIPPY_STATE, state)
            bundle.putSerializable(DOCUMENT_TYPE, documentType)
            val fragment = WizardFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var state: ZippyActivity.ZippyState
    private lateinit var documentType: DocumentType
    private lateinit var apiClient: ApiClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wizard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiClient = ApiClient(Zippy.secret, Zippy.key, Zippy.host, context!!)

        state = arguments?.getSerializable(ZIPPY_STATE) as? ZippyActivity.ZippyState
                ?: throw IllegalArgumentException("State was not passed to WizardFragment!")
        documentType = arguments?.getSerializable(DOCUMENT_TYPE) as? DocumentType
                ?: throw IllegalArgumentException("Document type was not passed to WizardFragment!")

        adjustViews()

        helpLabelTv.setOnClickListener{
            zippyBtn.visibility = View.INVISIBLE
            exampleIv.visibility = View.VISIBLE
        }

        exampleIv.setOnClickListener {
            exampleIv.visibility = View.GONE
            zippyBtn.visibility = View.VISIBLE
        }

        processAccordingToState()
    }

    private fun adjustViews() {
        if (documentType.value == ZippyActivity.DocumentMode.PASSPORT.value) {
            documentBackLabelTv.visibility = View.GONE
            docBackOkLabelTv.setText("")
        }

        var spannable = SpannableString("Need help with picture? Check out Picture example")
        spannable.setSpan(ForegroundColorSpan(Color.parseColor("#FF8700")), "Need help with picture? ".length, "Need help with picture? Check out Picture example".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        helpLabelTv.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    private fun processAccordingToState() {
        when(state) {
            ZippyActivity.ZippyState.LOADING -> {
                apiClient.getToken(object : AsyncResponse<String> {
                    override fun onSuccess(response: String) {
                        (activity as? ZippyActivity)?.state = ZippyActivity.ZippyState.READY
                        state = ZippyActivity.ZippyState.READY
                        processAccordingToState()
                    }

                    override fun onError(error: VolleyError) {
                        (activity as ZippyActivity).sendErrorResult("God damn it! Error!")
                    }
                })
            }
            ZippyActivity.ZippyState.READY -> {
                preparingOkLabelTv?.visibility = View.VISIBLE
                zippyBtn.text = "Take selfie"
                zippyBtn.setOnClickListener {
                    (activity as? ZippyActivity)?.onWizardNextStep(ZippyActivity.CameraMode.FACE)
                }
            }
            ZippyActivity.ZippyState.FACE_TAKEN -> {
                preparingOkLabelTv.visibility = View.VISIBLE
                faceOkLabelTv.visibility = View.VISIBLE
                zippyBtn.text = "Take document front"
                zippyBtn.setOnClickListener {
                    (activity as? ZippyActivity)?.onWizardNextStep(ZippyActivity.CameraMode.DOCUMENT_FRONT)
                }
            }
            ZippyActivity.ZippyState.DOC_FRONT_TAKEN -> {
                preparingOkLabelTv.visibility = View.VISIBLE
                faceOkLabelTv.visibility = View.VISIBLE
                docFrontOkLabelTv.visibility = View.VISIBLE
                zippyBtn.text = "Take document back"
                zippyBtn.setOnClickListener {
                    (activity as? ZippyActivity)?.onWizardNextStep(ZippyActivity.CameraMode.DOCUMENT_BACK)
                }
            }
            ZippyActivity.ZippyState.READY_TO_SEND -> {
                preparingOkLabelTv.visibility = View.VISIBLE
                faceOkLabelTv.visibility = View.VISIBLE
                docFrontOkLabelTv.visibility = View.VISIBLE
                docBackOkLabelTv.visibility = View.VISIBLE
                zippyBtn.text = "Send info!"
                zippyBtn.setOnClickListener {
                    (activity as ZippyActivity).sendImages(apiClient, documentType)
                    sendingOkLabelTv.visibility = View.VISIBLE
                    zippyBtn.text = "Processing..."
                }
            }
        }
    }
}