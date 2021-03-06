package com.zippyid.zippydroid.camera.helpers

import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face

class GraphicFaceTrackerFactory: MultiProcessor.Factory<Face> {
    override fun create(face: Face): Tracker<Face> {
        return GraphicFaceTracker()
    }
}