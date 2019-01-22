package com.zippyid.zippydroiddemo

import android.app.Application
import com.zippyid.zippydroid.Zippy

class ZippyDemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Zippy.initialize(
            "db32a431113a3ecc2f322c5a42dfb9b454e83ece5d78a83815373885070c7d75d41f3012b64d30b3",
            "0e1f18ff5fca8a9f0d971dd0b8e0dce0"
        )
    }
}