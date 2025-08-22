package com.bankingsystem.mobile.data.repository


import android.content.Context
import android.net.Uri
import com.bankingsystem.mobile.data.model.kyc.KycSubmitBody
import com.bankingsystem.mobile.data.model.kyc.KycSubmitResponse
import com.bankingsystem.mobile.data.service.KycApi
import com.bankingsystem.mobile.ui.kyc.KycUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.source
import retrofit2.Response


@Singleton
class KycRepository @Inject constructor(
    private val api: KycApi,
    @ApplicationContext private val ctx: Context
) {
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
        val ext = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val body = requestBodyFromUri(uri)
        return MultipartBody.Part.createFormData("file", "$name.$ext", body)
    }


    suspend fun upload(uri: Uri, type: String) =
        api.uploadImage(partFromUri(uri, type), type.toRequestBody("text/plain".toMediaType()))


    suspend fun submit(all: KycUiState, ids: Map<String, String>): Response<KycSubmitResponse> =
        api.submit(
            KycSubmitBody(
                docFrontId = ids.getValue("DOC_FRONT"),
                docBackId = ids.getValue("DOC_BACK"),
                selfieId = ids.getValue("SELFIE"),
                addressId = ids.getValue("ADDRESS_PROOF"),
                consent = all.consentAccepted
            )
        )
    suspend fun myCase() = api.me()
    suspend fun myChecks(caseId: String) = api.checks(caseId)
}
