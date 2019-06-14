package com.zippyid.zippydroiddemo

import android.app.Application
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.network.model.ZippyCallback

class ZippyDemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Zippy.initialize("96d648cb194cd2e085cff4c2c2860ae7d83984398ab8020e66782116e4ab4d01bb900a73e86bf5fa")
    }
}