package com.zippyid.zippydroiddemo

import android.app.Application
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.network.model.ZippyCallback

class ZippyDemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Zippy.initialize(
            "db32a431113a3ecc2f322c5a42dfb9b454e83ece5d78a83815373885070c7d75d41f3012b64d30b3",
            "0e1f18ff5fca8a9f0d971dd0b8e0dce0"
        )

        var callback: ZippyCallback = object:ZippyCallback {
            override fun onSubmit() {
                //  fired, tad kad ir uploadotas visas bildes
            }
            override fun onTextExtracted() {
                // fired, tad kad API end-pointā parādās rezultāts ( iedošu endpoint )
            }

            override fun onFinished() {
                // fired, tad kad visi checki veikti (API end point jau it kā ir, bet uztaisīšu mazliet sakarīgāku)
            }
        }

        Zippy.createCallback(callback)
    }
}