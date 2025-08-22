package com.bankingsystem.mobile.data.model.kyc

data class KycCaseStatusResponse(
    val caseId: String,
    val status: String,
    val decisionReason: String? = null
)