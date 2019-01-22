package com.zippyid.zippydroid.network.model
import com.google.gson.annotations.SerializedName

data class SuccessResponse(
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
    val images: Images?,
    @SerializedName("processing_errors")
    val processingErrors: ProcessingErrors?,
    @SerializedName("similar_faces")
    val similarFaces: SimilarFaces?,
    @SerializedName("state")
    val state: String?
)

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
)

data class Images(
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
)

data class ProcessingErrors(
    @SerializedName("text_extraction")
    val textExtraction: String?
)

class SimilarFaces