package com.zippyid.zippydroiddemo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.zippyid.zippydroid.ZippyActivity
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

    fun launchZippy() {
        val intent = Intent(this, ZippyActivity::class.java)

        startActivityForResult(intent, ZIPPY_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ZIPPY_RESULT_CODE) {
            val result = data?.getStringExtra(ZippyActivity.ZIPPY_RESULT) ?: "No zippy result :/"
            result_tv.text = result
        }
    }
}
