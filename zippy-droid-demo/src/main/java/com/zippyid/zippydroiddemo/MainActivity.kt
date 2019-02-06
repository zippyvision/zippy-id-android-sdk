package com.zippyid.zippydroiddemo

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.network.model.DocumentType
import com.zippyid.zippydroid.network.model.SessionConfig
import com.zippyid.zippydroid.network.model.ZippyResponse
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val ZIPPY_RESULT_CODE = 719
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openZippyBtn.setOnClickListener {
            launchZippy()
        }
    }

    private fun launchZippy() {
        val intent = Intent(this, ZippyActivity::class.java)
        intent.putExtra("SESSION_CONFIGURATION", getSessionConfiguration())
        startActivityForResult(intent, ZIPPY_RESULT_CODE)
    }

    private fun getSessionConfiguration(): SessionConfig {
        val dateLong = System.currentTimeMillis() / 1000
        val date = dateLong.toString()

        return SessionConfig(customerId = date, documentType = DocumentType("ID card", "id_card"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ZIPPY_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data?.getParcelableExtra<ZippyResponse>(ZippyActivity.ZIPPY_RESULT)
                Log.e("ZIPPY", "${result?.state}, ${result?.processingErrors?.textExtraction}, ${result?.createdAt}")
                result_tv.text = when (result?.state) {
                    "finished" -> result.state
                    "failed" -> result.processingErrors?.textExtraction ?: "Unknown processing error"
                    else -> "Error"
                }
            } else {
                result_tv.text = data?.getStringExtra(ZippyActivity.ZIPPY_RESULT)
            }
        }
    }
}
