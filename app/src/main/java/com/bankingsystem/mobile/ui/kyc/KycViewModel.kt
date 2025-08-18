package com.bankingsystem.mobile.ui.kyc

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bankingsystem.mobile.data.repository.KycRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class KycViewModel @Inject constructor(
    private val repo: KycRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(KycUiState())
    val ui: StateFlow<KycUiState> = _ui.asStateFlow()

    private val _uploadedIds = MutableStateFlow<Map<String, String>>(emptyMap())
    val uploadedIds: StateFlow<Map<String, String>> = _uploadedIds.asStateFlow()

    private val _uploading = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> = _uploading.asStateFlow()

    fun go(step: KycStep) = _ui.update { it.copy(step = step) }

    fun next() = _ui.update {
        val s = when (it.step) {
            KycStep.Document -> KycStep.Selfie
            KycStep.Selfie -> KycStep.Address
            KycStep.Address -> KycStep.Review
            KycStep.Review -> KycStep.Review
        }
        it.copy(step = s)
    }

    fun back() = _ui.update {
        val s = when (it.step) {
            KycStep.Document -> KycStep.Document
            KycStep.Selfie -> KycStep.Document
            KycStep.Address -> KycStep.Selfie
            KycStep.Review -> KycStep.Address
        }
        it.copy(step = s)
    }

    fun setDocFront(uri: Uri?) { _ui.update { it.copy(docFront = uri) }; if (uri != null) upload("doc_front", uri) }
    fun setDocBack(uri: Uri?)  { _ui.update { it.copy(docBack = uri)  }; if (uri != null) upload("doc_back", uri)  }
    fun setSelfie(uri: Uri?)   { _ui.update { it.copy(selfie = uri)   }; if (uri != null) upload("selfie", uri)    }
    fun setAddressProof(uri: Uri?) { _ui.update { it.copy(addressProof = uri) }; if (uri != null) upload("address", uri) }

    fun setDocQuality(q: DocQuality) = _ui.update { it.copy(docQuality = q) }
    fun setOcrFields(fields: List<OcrField>) = _ui.update { it.copy(ocrFields = fields) }
    fun setLiveness(score: Float?) = _ui.update { it.copy(livenessScore = score) }
    fun setFaceMatch(score: Float?) = _ui.update { it.copy(faceMatchScore = score) }
    fun setConsent(accepted: Boolean) = _ui.update { it.copy(consentAccepted = accepted) }

    private fun upload(type: String, uri: Uri) {
        viewModelScope.launch {
            _uploading.value = true
            runCatching { repo.upload(uri, type) }
                .onSuccess { part -> _uploadedIds.update { it + (type to part.id) } }
            _uploading.value = false
        }
    }

    fun canContinueFromDocument(): Boolean {
        val ok = ui.value.docFront != null && ui.value.docBack != null
        val q = ui.value.docQuality
        val blurOk = (q.blurScore ?: 1f) >= 0.30f
        val glareOk = (q.glareScore ?: 1f) >= 0.60f
        val cornersOk = (q.cornerCoverage ?: 0) >= 3
        return ok && blurOk && glareOk && cornersOk
    }

    fun canContinueFromSelfie(): Boolean {
        val liveOk = (ui.value.livenessScore ?: 0f) >= 0.60f
        val matchOk = (ui.value.faceMatchScore ?: 0f) >= 0.60f
        return ui.value.selfie != null && liveOk && matchOk
    }

    fun canContinueFromAddress(): Boolean = ui.value.addressProof != null
    fun canSubmit(): Boolean = ui.value.consentAccepted

    fun docsOk() = ui.value.docFront != null && ui.value.docBack != null
    fun selfieOk(): Boolean {
        val liveOk = (ui.value.livenessScore ?: 1f) >= 0.60f
        val matchOk = (ui.value.faceMatchScore ?: 1f) >= 0.60f
        return ui.value.selfie != null && liveOk && matchOk
    }
    fun addressOk() = ui.value.addressProof != null

    fun readyToSubmit(strict: Boolean = true): Boolean =
        if (strict) {
            docsOk() && selfieOk() && addressOk() && ui.value.consentAccepted
        } else {
            listOf(docsOk(), selfieOk(), addressOk()).count { it } >= 2 && ui.value.consentAccepted
        }

    suspend fun submit(): Boolean {
        if (!readyToSubmit(strict = true)) return false
        val ids = uploadedIds.value
        if (!listOf("doc_front","doc_back","selfie","address").all { it in ids }) return false
        return runCatching { repo.submit(ui.value, ids) }
            .map { it.status.equals("ok", true) || it.status.equals("submitted", true) }
            .getOrDefault(false)
    }
}
