package com.zippyid.zippydroid.camera.helpers

import android.util.Log
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode

class BarcodeTracker internal constructor() : Tracker<Barcode>() {
    companion object {
        var mBarcodeDetectorListener: BarcodeDetectorListener? = null
    }

    override fun onNewItem(p0: Int, p1: Barcode?) {
        super.onNewItem(p0, p1)
        Log.e("DRIVERS_L", "WHY DONT I GET HERE")
        if(mBarcodeDetectorListener == null) return
        mBarcodeDetectorListener!!.onBarcodeDetected(p1!!)
        mBarcodeDetectorListener = null
    }

    interface  BarcodeDetectorListener {
        fun onBarcodeDetected(barcode: Barcode)
    }
}