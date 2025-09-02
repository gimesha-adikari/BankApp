package com.bankingsystem.mobile.features.kyc.integration.repository

import android.content.Context
import android.net.Uri
import com.bankingsystem.mobile.features.kyc.domain.model.*
import com.bankingsystem.mobile.features.kyc.domain.repository.KycRepository
import com.bankingsystem.mobile.features.kyc.integration.remote.api.KycApi
import com.bankingsystem.mobile.features.kyc.integration.remote.dto.KycSubmitBody
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.source

@Singleton
class KycRepositoryImpl @Inject constructor(
    private val api: KycApi,
    @ApplicationContext private val ctx: Context
) : KycRepository {

    private fun requestBodyFromUri(uri: Uri): RequestBody {
        val resolver = ctx.contentResolver
        val mime = resolver.getType(uri) ?: "image/jpeg"
        return object : RequestBody() {
            override fun contentType() = mime.toMediaType()
            override fun writeTo(sink: okio.BufferedSink) {
                resolver.openInputStream(uri)?.use { input -> sink.writeAll(input.source()) }
            }
        }
    }

    private fun partFromUri(uri: Uri, name: String): MultipartBody.Part {
        val resolver = ctx.contentResolver
        val mime = resolver.getType(uri)?.lowercase() ?: "image/jpeg"
        val ext = when (mime) { "image/png" -> "png"; "image/webp" -> "webp"; else -> "jpg" }
        val body = requestBodyFromUri(uri)
        return MultipartBody.Part.createFormData("file", "$name.$ext", body)
    }

    override suspend fun upload(uri: Uri, type: String): UploadedPart {
        val dto = api.uploadImage(
            partFromUri(uri, type),
            type.toRequestBody("text/plain".toMediaType())
        )
        return UploadedPart(dto.id)
    }

    override suspend fun submit(consent: Boolean, ids: KycUploadIds): KycSubmitResponse {
        val resp = api.submit(
            KycSubmitBody(
                docFrontId = ids.docFrontId,
                docBackId  = ids.docBackId,
                selfieId   = ids.selfieId,
                addressId  = ids.addressId,
                consent    = consent
            )
        )
        if (!resp.isSuccessful || resp.body() == null) {
            throw IllegalStateException(resp.errorBody()?.string() ?: "Submit failed")
        }
        val body = resp.body()!!
        return KycSubmitResponse(caseId = body.caseId, status = body.status)
    }

    override suspend fun myCase(): KycCaseStatus {
        val d = api.me()
        return KycCaseStatus(d.caseId, d.status, d.decisionReason)
    }

    override suspend fun myChecks(caseId: String): List<KycCheck> =
        api.checks(caseId).map { KycCheck(it.type, it.score, it.passed) }
}
