package com.zippyid.zippydroid.network.model

import android.os.Parcel
import android.os.Parcelable

class SimilarFaces() : Parcelable {
    constructor(parcel: Parcel) : this()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SimilarFaces> {
        override fun createFromParcel(parcel: Parcel): SimilarFaces {
            return SimilarFaces(parcel)
        }

        override fun newArray(size: Int): Array<SimilarFaces?> {
            return arrayOfNulls(size)
        }
    }
}