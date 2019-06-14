package com.zippyid.zippydroiddemo

import android.app.Application
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.network.model.ZippyCallback

class ZippyDemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Zippy.initialize(
            "96d648cb194cd2e085cff4c2c2860ae7d83984398ab8020e66782116e4ab4d01bb900a73e86bf5fa",
            "1",
            object:ZippyCallback {
            override fun onSubmit() {
                // Callback, which fires when user has finished uploading images. Use it to send a loading spinner or inform user, that request is being processed.
            }
            override fun onTextExtracted() {
                // Callback, which fires when user has finished uploading images. Use it to send a loading spinner or inform user, that request is being processed.
            }

            override fun onFinished() {
                // Callback, which fires when user has finished uploading images. Use it to send a loading spinner or inform user, that request is being processed.
            }
        },
            object:ZippyCallback {
                override fun onSubmit() {
                    // Callback, which fires after verification process has been completed. `data` parameter contains customer's ID card data, which you can use to autofill forms or however you see fit.
                }
                override fun onTextExtracted() {
                    // Callback, which fires after verification process has been completed. `data` parameter contains customer's ID card data, which you can use to autofill forms or however you see fit.
                }

                override fun onFinished() {
                    // Callback, which fires after verification process has been completed. `data` parameter contains customer's ID card data, which you can use to autofill forms or however you see fit.
                }
            }
        )
    }
}