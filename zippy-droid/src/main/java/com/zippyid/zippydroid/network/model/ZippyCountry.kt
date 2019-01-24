package com.zippyid.zippydroid.network.model
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Country(
    @SerializedName("value")
    val value: String?,
    @SerializedName("label")
    val label: String?,
    @SerializedName("document_types")
    val documentTypes: List<DocumentType>? = null
)

data class DocumentType(
    @SerializedName("value")
    val value: String?,
    @SerializedName("label")
    val label: String?
) : Serializable
