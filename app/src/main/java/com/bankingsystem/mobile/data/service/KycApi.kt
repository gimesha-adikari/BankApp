package com.bankingsystem.mobile.data.service

import com.bankingsystem.mobile.data.model.kyc.KycCaseStatusResponse
import com.bankingsystem.mobile.data.model.kyc.KycCheckDto
import com.bankingsystem.mobile.data.model.kyc.KycSubmitBody
import com.bankingsystem.mobile.data.model.kyc.KycSubmitResponse
import com.bankingsystem.mobile.data.model.kyc.UploadedPart
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface KycApi {

    @Multipart
    @POST("kyc/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody // "DOC_FRONT","DOC_BACK","SELFIE","ADDRESS_PROOF"
    ): UploadedPart // { id: String }

    @POST("kyc/submit")
    suspend fun submit(@Body body: KycSubmitBody): retrofit2.Response<KycSubmitResponse>

    @GET("kyc/me")
    suspend fun me(): KycCaseStatusResponse

    @GET("kyc/{id}/checks")
    suspend fun checks(@Path("id") caseId: String): List<KycCheckDto>
}
