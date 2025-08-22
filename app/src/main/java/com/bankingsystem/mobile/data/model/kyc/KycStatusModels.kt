package com.bankingsystem.mobile.data.model.kyc


data class KycCaseDto(
    val caseId: String,
    val status: String,
    val decisionReason: String? = null,
    val createdAt: String,
    val updatedAt: String
)


data class KycCheckDto(
    val id: String,
    val type: String,
    val score: Double?,
    val passed: Boolean?,
    val createdAt: String
)