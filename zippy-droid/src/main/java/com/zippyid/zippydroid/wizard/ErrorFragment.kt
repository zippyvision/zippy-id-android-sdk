package com.zippyid.zippydroid.wizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.databinding.FragmentErrorBinding
import com.zippyid.zippydroid.viewModel.ZippyState
import com.zippyid.zippydroid.viewModel.ZippyViewModel
import com.zippyid.zippydroid.viewModel.ZippyViewModelFactory

class ErrorFragment: Fragment()  {
    private lateinit var viewModelFactory: ZippyViewModelFactory
    private lateinit var viewModel: ZippyViewModel

    private lateinit var binding: FragmentErrorBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentErrorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModelFactory = ZippyViewModelFactory(context!!, (activity as ZippyActivity).getConfig())
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(ZippyViewModel::class.java)

        val errorMessage = viewModel.verificationStateAndZippyResult.value?.first?.error
        binding.descriptionTv.text =
            if (errorMessage != null) resources.getString(R.string.reason_for_failure, errorMessage)
            else resources.getString(R.string.reason_unknown)

        binding.retryBtn.setOnClickListener {
            viewModel.setZippyState(ZippyState.RETRY)
            findNavController().navigate(R.id.action_errorFragment_to_idVerificationFragment)
        }
    }
}