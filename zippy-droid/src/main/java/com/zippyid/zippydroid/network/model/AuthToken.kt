package com.zippyid.zippydroid.network.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class AuthToken(
    @SerializedName("token")
    val token: String
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(token)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProcessingErrors> {
        override fun createFromParcel(parcel: Parcel): ProcessingErrors {
            return ProcessingErrors(parcel)
        }

        override fun newArray(size: Int): Array<ProcessingErrors?> {
            return arrayOfNulls(size)
        }
    }
}