package com.zippyid.zippydroid.network.model
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ZippyResponse(
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("customer_data")
    val customerData: CustomerData?,
    @SerializedName("face_id")
    val faceId: String?,
    @SerializedName("face_similarity")
    val faceSimilarity: String?,
    @SerializedName("finished_at")
    val finishedAt: String?,
    @SerializedName("images")
    val images: ZippyImages?,
    @SerializedName("processing_errors")
    val processingErrors: ProcessingErrors?,
    @SerializedName("similar_faces")
    val similarFaces: SimilarFaces?,
    @SerializedName("state")
    val state: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(CustomerData::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(ZippyImages::class.java.classLoader),
        parcel.readParcelable(ProcessingErrors::class.java.classLoader),
        parcel.readParcelable(SimilarFaces::class.java.classLoader),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(createdAt)
        parcel.writeParcelable(customerData, flags)
        parcel.writeString(faceId)
        parcel.writeString(faceSimilarity)
        parcel.writeString(finishedAt)
        parcel.writeParcelable(images, flags)
        parcel.writeParcelable(processingErrors, flags)
        parcel.writeParcelable(similarFaces, flags)
        parcel.writeString(state)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ZippyResponse> {
        override fun createFromParcel(parcel: Parcel): ZippyResponse {
            return ZippyResponse(parcel)
        }

        override fun newArray(size: Int): Array<ZippyResponse?> {
            return arrayOfNulls(size)
        }
    }
}