package com.zippyid.zippydroid.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.network.ApiClient
import com.zippyid.zippydroid.network.model.SessionConfig

class ZippyViewModelFactory(private val context: Context, private val configuration: SessionConfig): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ZippyViewModel(ApiClient(Zippy.token, Zippy.host, context), configuration) as T
    }
}