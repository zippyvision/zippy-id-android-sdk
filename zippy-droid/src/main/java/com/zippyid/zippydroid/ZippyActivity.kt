package com.zippyid.zippydroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.android.volley.VolleyError
import com.zippyid.zippydroid.extension.observeLiveData
import com.zippyid.zippydroid.network.model.SessionConfig
import com.zippyid.zippydroid.network.model.ZippyResponse
import com.zippyid.zippydroid.viewModel.*

class ZippyActivity : AppCompatActivity() {
    companion object {
        const val ZIPPY_RESULT = "zippy_result"
    }

    lateinit var viewModelFactory: ZippyViewModelFactory
    private lateinit var viewModel: ZippyViewModel

    private lateinit var navController: NavController
    private var sessionConfiguration: SessionConfig? = null

    fun getConfig(): SessionConfig {
        return sessionConfiguration ?: intent.getParcelableExtra("SESSION_CONFIGURATION")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zippy)
        navController = findNavController(R.id.zippyNavFragment)

        viewModelFactory = ZippyViewModelFactory(this, getConfig())
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ZippyViewModel::class.java)
        observeLiveData()
    }

    private fun observeLiveData() {
        this.observeLiveData(viewModel.sendSuccessfulResultLiveData) {
            sendSuccessfulResult(it)
        }
        this.observeLiveData(viewModel.sendCanceledResultLiveData) {
            sendCancelledResult(it)
        }
        this.observeLiveData(viewModel.sendErrorResultLiveData) {
            sendErrorResult(it)
        }
    }

    fun sendSuccessfulResult(response: ZippyResponse) {
        val returnIntent = Intent()
            .apply { putExtra(ZIPPY_RESULT, response) }
        Zippy.callback?.onFinished()
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    fun sendErrorResult(message: String) {
        val returnIntent = Intent()
        returnIntent.putExtra(ZIPPY_RESULT, message)
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    fun sendCancelledResult(error: VolleyError?) {
        setResult(Activity.RESULT_CANCELED, null)
        Intent().apply {
            error?.apply {
                putExtra(ZIPPY_RESULT, this.localizedMessage)
            } ?: putExtra(ZIPPY_RESULT, "Request timed out")
        }
        finish()
    }
}