package com.zippyid.zippydroid.network.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Country(
    @SerializedName("value")
    val value: String?,
    @SerializedName("label")
    val label: String?,
    @SerializedName("document_types")
    val documentTypes: List<DocumentType>? = null
)

data class DocumentType(
    @SerializedName("label")
    val label: String?,
    @SerializedName("value")
    val value: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(label)
        parcel.writeString(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DocumentType> {
        override fun createFromParcel(parcel: Parcel): DocumentType {
            return DocumentType(parcel)
        }

        override fun newArray(size: Int): Array<DocumentType?> {
            return arrayOfNulls(size)
        }
    }
}
