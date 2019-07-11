package com.zippyid.zippydroid.network.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class SessionConfig(
    @SerializedName("customer_uid")
    val customerId: String,
    @SerializedName("document_type")
    var documentType: DocumentType
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(DocumentType::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(customerId)
        parcel.writeParcelable(documentType, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SessionConfig> {
        override fun createFromParcel(parcel: Parcel): SessionConfig {
            return SessionConfig(parcel)
        }

        override fun newArray(size: Int): Array<SessionConfig?> {
            return arrayOfNulls(size)
        }
    }
}