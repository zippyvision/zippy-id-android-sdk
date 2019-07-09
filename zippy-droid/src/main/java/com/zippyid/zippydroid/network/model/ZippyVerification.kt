package com.zippyid.zippydroid.network.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ZippyVerification (
    @SerializedName("state")
    val state: String?,
    @SerializedName("error")
    val error: String?,
    @SerializedName("request_token")
    val requestToken: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(state)
        parcel.writeString(error)
        parcel.writeString(requestToken)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ZippyVerification> {
        override fun createFromParcel(parcel: Parcel): ZippyVerification {
            return ZippyVerification(parcel)
        }

        override fun newArray(size: Int): Array<ZippyVerification?> {
            return arrayOfNulls(size)
        }
    }
}