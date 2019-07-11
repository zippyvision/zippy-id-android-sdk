package com.zippyid.zippydroid.wizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.viewModel.ZippyState
import com.zippyid.zippydroid.viewModel.ZippyViewModel
import com.zippyid.zippydroid.viewModel.ZippyViewModelFactory
import kotlinx.android.synthetic.main.fragment_error.*

class ErrorFragment: Fragment()  {
    lateinit var viewModelFactory: ZippyViewModelFactory
    private lateinit var viewModel: ZippyViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_error, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModelFactory = ZippyViewModelFactory(context!!, (activity as ZippyActivity).getConfig())
        viewModel = ViewModelProviders.of((activity as ZippyActivity), viewModelFactory).get(ZippyViewModel::class.java)

        val errorMessage = viewModel.verification?.error
        descriptionTv.text = if (errorMessage != null) resources.getString(R.string.reason_for_failure, errorMessage) else resources.getString(R.string.reason_unknown)

        viewModel.verification?.requestToken?.apply {
            viewModel.applyNewToken(this)
        }

        retryBtn.setOnClickListener {
            viewModel.setZippyState(ZippyState.RETRY)
            (activity as? ZippyActivity)?.toIDVerificationFragment()
        }
    }
}