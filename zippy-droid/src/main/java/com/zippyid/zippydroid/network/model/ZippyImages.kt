package com.zippyid.zippydroid.network.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ZippyImages(
    @SerializedName("id_back")
    val idBack: String?,
    @SerializedName("id_front")
    val idFront: String?,
    @SerializedName("payslip")
    val payslip: String?,
    @SerializedName("proof_of_residence")
    val proofOfResidence: String?,
    @SerializedName("selfie")
    val selfie: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idBack)
        parcel.writeString(idFront)
        parcel.writeString(payslip)
        parcel.writeString(proofOfResidence)
        parcel.writeString(selfie)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ZippyImages> {
        override fun createFromParcel(parcel: Parcel): ZippyImages {
            return ZippyImages(parcel)
        }

        override fun newArray(size: Int): Array<ZippyImages?> {
            return arrayOfNulls(size)
        }
    }
}