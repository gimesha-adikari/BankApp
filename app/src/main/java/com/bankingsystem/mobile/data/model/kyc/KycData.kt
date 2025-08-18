package com.bankingsystem.mobile.data.model.kyc

data class UploadedPart(val id: String)

data class KycSubmitBody(
    val docFrontId: String,
    val docBackId: String,
    val selfieId: String,
    val addressId: String,
    val consent: Boolean
)

data class KycSubmitResponse(val status: String)
