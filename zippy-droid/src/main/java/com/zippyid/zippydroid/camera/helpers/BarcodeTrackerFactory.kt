package com.zippyid.zippydroid.camera.helpers

import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode

class BarcodeTrackerFactory: MultiProcessor.Factory<Barcode> {
    override fun create(barcode: Barcode): Tracker<Barcode> {
        return BarcodeTracker()
    }
}