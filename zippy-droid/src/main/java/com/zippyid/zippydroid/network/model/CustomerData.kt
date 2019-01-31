package com.zippyid.zippydroid.network.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class CustomerData(
    @SerializedName("address")
    val address: String?,
    @SerializedName("birth_date")
    val birthDate: String?,
    @SerializedName("birth_place")
    val birthPlace: String?,
    @SerializedName("blood_type")
    val bloodType: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("document_date")
    val documentDate: String?,
    @SerializedName("document_issuer")
    val documentIssuer: String?,
    @SerializedName("document_number")
    val documentNumber: String?,
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("last_name")
    val lastName: String?,
    @SerializedName("marital_status")
    val maritalStatus: String?,
    @SerializedName("middle_name")
    val middleName: String?,
    @SerializedName("nationality")
    val nationality: String?,
    @SerializedName("occupation")
    val occupation: String?,
    @SerializedName("personal_code")
    val personalCode: String?,
    @SerializedName("province_1")
    val province1: String?,
    @SerializedName("province_2")
    val province2: String?,
    @SerializedName("religion")
    val religion: String?,
    @SerializedName("state")
    val state: String?,
    @SerializedName("validity_date")
    val validityDate: String?,
    @SerializedName("zip_code")
    val zipCode: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(address)
        parcel.writeString(birthDate)
        parcel.writeString(birthPlace)
        parcel.writeString(bloodType)
        parcel.writeString(city)
        parcel.writeString(documentDate)
        parcel.writeString(documentIssuer)
        parcel.writeString(documentNumber)
        parcel.writeString(firstName)
        parcel.writeString(fullName)
        parcel.writeString(gender)
        parcel.writeString(lastName)
        parcel.writeString(maritalStatus)
        parcel.writeString(middleName)
        parcel.writeString(nationality)
        parcel.writeString(occupation)
        parcel.writeString(personalCode)
        parcel.writeString(province1)
        parcel.writeString(province2)
        parcel.writeString(religion)
        parcel.writeString(state)
        parcel.writeString(validityDate)
        parcel.writeString(zipCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CustomerData> {
        override fun createFromParcel(parcel: Parcel): CustomerData {
            return CustomerData(parcel)
        }

        override fun newArray(size: Int): Array<CustomerData?> {
            return arrayOfNulls(size)
        }
    }

}