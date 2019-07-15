package com.zippyid.zippydroid.network.model

import com.google.gson.annotations.SerializedName

data class IdModel(
    @SerializedName("verification_id")
    val verificationId: String,
    @SerializedName("customer_uid")
    val customerUid: String
)