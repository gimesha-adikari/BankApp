package com.bankingsystem.mobile.ui.kyc

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class KycViewModel : ViewModel() {

    private val _ui = MutableStateFlow(KycUiState())
    val ui: StateFlow<KycUiState> = _ui.asStateFlow()

    /* -------- navigation between steps -------- */

    fun go(step: KycStep) = _ui.update { it.copy(step = step) }

    fun next() = _ui.update {
        val s = when (it.step) {
            KycStep.Document -> KycStep.Selfie
            KycStep.Selfie   -> KycStep.Address
            KycStep.Address  -> KycStep.Review
            KycStep.Review   -> KycStep.Review
        }
        it.copy(step = s)
    }

    fun back() = _ui.update {
        val s = when (it.step) {
            KycStep.Document -> KycStep.Document
            KycStep.Selfie   -> KycStep.Document
            KycStep.Address  -> KycStep.Selfie
            KycStep.Review   -> KycStep.Address
        }
        it.copy(step = s)
    }

    /* -------- document state -------- */

    fun setDocFront(uri: Uri?) = _ui.update { it.copy(docFront = uri) }
    fun setDocBack(uri: Uri?)  = _ui.update { it.copy(docBack = uri) }
    fun setDocQuality(q: DocQuality) = _ui.update { it.copy(docQuality = q) }
    fun setOcrFields(fields: List<OcrField>) = _ui.update { it.copy(ocrFields = fields) }

    /* -------- selfie / scores -------- */

    fun setSelfie(uri: Uri?) = _ui.update { it.copy(selfie = uri) }
    fun setLiveness(score: Float?) = _ui.update { it.copy(livenessScore = score) }
    fun setFaceMatch(score: Float?) = _ui.update { it.copy(faceMatchScore = score) }

    /* -------- address -------- */

    fun setAddressProof(uri: Uri?) = _ui.update { it.copy(addressProof = uri) }

    /* -------- review -------- */

    fun setConsent(accepted: Boolean) = _ui.update { it.copy(consentAccepted = accepted) }

    /* -------- helpers for enabling buttons -------- */

    fun canContinueFromDocument(): Boolean = ui.value.docFront != null && ui.value.docBack != null
    fun canContinueFromSelfie(): Boolean = ui.value.selfie != null
    fun canContinueFromAddress(): Boolean = ui.value.addressProof != null
    fun canSubmit(): Boolean = ui.value.consentAccepted
}
