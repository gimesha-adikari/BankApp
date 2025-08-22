package com.bankingsystem.mobile.ui.kyc

import com.bankingsystem.mobile.data.model.kyc.KycCaseStatusResponse
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bankingsystem.mobile.data.model.kyc.KycCheckDto
import com.bankingsystem.mobile.data.repository.KycRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
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

    private val _status = MutableStateFlow<KycCaseStatusResponse?>(null)
    val status: StateFlow<KycCaseStatusResponse?> = _status.asStateFlow()

    private val _checks = MutableStateFlow<List<KycCheckDto>>(emptyList())
    val checks: StateFlow<List<KycCheckDto>> = _checks.asStateFlow()

    fun refreshStatusOnce() = viewModelScope.launch {
        val st = runCatching { repo.myCase() }.getOrNull()
        _status.value = st
        if (st != null) {
            val ch = runCatching { repo.myChecks(st.caseId) }.getOrElse { emptyList() }
            _checks.value = ch
        }
    }

    private var pollJob: Job? = null

    fun startPollingStatus(intervalMs: Long = 3000L) {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                val st = runCatching { repo.myCase() }.getOrNull()
                _status.value = st
                if (st != null) {
                    val ch = runCatching { repo.myChecks(st.caseId) }.getOrElse { emptyList() }
                    _checks.value = ch
                }

                val s = _status.value?.status
                if (s == "APPROVED" || s == "REJECTED" || s == "NEEDS_MORE_INFO") break
                delay(intervalMs)
            }
        }
    }

    fun stopPollingStatus() {
        pollJob?.cancel()
        pollJob = null
    }

    fun pollStatusUntilTerminal(intervalMs: Long = 3000L) = startPollingStatus(intervalMs)


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

    fun setDocFront(uri: Uri?) { _ui.update { it.copy(docFront = uri) }; if (uri != null) upload("DOC_FRONT", uri) }
    fun setDocBack(uri: Uri?)  { _ui.update { it.copy(docBack = uri)  }; if (uri != null) upload("DOC_BACK", uri)  }
    fun setSelfie(uri: Uri?)   { _ui.update { it.copy(selfie = uri)   }; if (uri != null) upload("SELFIE", uri)    }
    fun setAddressProof(uri: Uri?) { _ui.update { it.copy(addressProof = uri) }; if (uri != null) upload("ADDRESS_PROOF", uri) }

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
        val blurOk = (q.blurScore ?: 1f) >= 0.10f
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
        Log.d("KYC", "submit(): invoked")

        if (!readyToSubmit(strict = true)) {
            Log.w("KYC", "submit(): not ready. docs=${docsOk()} selfie=${selfieOk()} addr=${addressOk()} consent=${ui.value.consentAccepted}")
            return false
        }

        val ids = uploadedIds.value
        val missing = listOf("DOC_FRONT","DOC_BACK","SELFIE","ADDRESS_PROOF").filterNot { it in ids }
        if (missing.isNotEmpty()) {
            Log.w("KYC", "submit(): missing upload ids: $missing ; map=$ids")
            return false
        }

        return try {
            val resp = repo.submit(ui.value, ids)

            val code = resp.code()
            val body = resp.body()
            Log.d("KYC", "submit(): http=$code success=${resp.isSuccessful} body=$body")

            if (!resp.isSuccessful) {
                Log.w("KYC", "submit(): errorBody=${resp.errorBody()?.string()}")
                return false
            }

            val okStatuses = setOf("PENDING","AUTO_REVIEW","UNDER_REVIEW","APPROVED","REJECTED","NEEDS_MORE_INFO")
            val looksOk = body?.status?.trim()?.uppercase() in okStatuses
            Log.d("KYC", "submit(): looksOk=$looksOk")
            looksOk
        } catch (e: Exception) {
            Log.e("KYC", "submit(): exception", e)
            false
        }
    }



}
