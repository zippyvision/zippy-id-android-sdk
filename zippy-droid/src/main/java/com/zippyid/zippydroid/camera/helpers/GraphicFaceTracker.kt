package com.zippyid.zippydroid.camera.helpers

import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face

class GraphicFaceTracker internal constructor() : Tracker<Face>() {
    companion object {
        var mFaceDetectorListener: FaceDetectorListener? = null
    }

    override fun onNewItem(faceId: Int, item: Face?) {
        if(mFaceDetectorListener == null) return
        mFaceDetectorListener!!.onFaceDetected(item!!)
        mFaceDetectorListener = null
    }

    interface  FaceDetectorListener {
        fun onFaceDetected(face: Face)
    }
}