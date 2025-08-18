package com.bankingsystem.mobile.ui.kyc

import android.net.Uri

/** Keep UI steps in the order you want the flow to advance. */
enum class KycStep { Document, Selfie, Address, Review }

data class DocQuality(
    val blurScore: Float? = null,      // 0..1 (1 = sharp)
    val glareScore: Float? = null,     // 0..1 (1 = no glare)
    val cornerCoverage: Int? = null    // 0..4 corners detected
)

data class OcrField(
    val label: String,
    val value: String,
    val confidence: Float? = null      // 0..1
)

data class KycUiState(
    val step: KycStep = KycStep.Document,

    // Document
    val docFront: Uri? = null,
    val docBack: Uri? = null,
    val docQuality: DocQuality = DocQuality(),
    val ocrFields: List<OcrField> = emptyList(),

    // Selfie
    val selfie: Uri? = null,
    val livenessScore: Float? = null,   // 0..1
    val faceMatchScore: Float? = null,  // 0..1

    // Address
    val addressProof: Uri? = null,

    // Review
    val consentAccepted: Boolean = false
)
