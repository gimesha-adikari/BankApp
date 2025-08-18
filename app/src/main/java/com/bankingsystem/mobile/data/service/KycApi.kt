package com.bankingsystem.mobile.data.service

import com.bankingsystem.mobile.data.model.kyc.KycSubmitBody
import com.bankingsystem.mobile.data.model.kyc.KycSubmitResponse
import com.bankingsystem.mobile.data.model.kyc.UploadedPart
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface KycApi {

    @Multipart
    @POST("kyc/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody // "doc_front","doc_back","selfie","address"
    ): UploadedPart // { id: String }

    @POST("kyc/submit")
    suspend fun submit(@Body body: KycSubmitBody): KycSubmitResponse
}
