package com.bankingsystem.mobile.data.remote

import com.bankingsystem.mobile.data.remote.dto.LoginRequest
import com.bankingsystem.mobile.data.remote.dto.LoginResponse
import com.bankingsystem.mobile.data.service.ApiService

class AuthApiImpl(
    private val api: ApiService
) : AuthApi {

    override suspend fun getMe(token: String): Result<com.bankingsystem.mobile.data.model.UserProfile> = try {
        val res = api.getMe()
        if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
        else Result.failure(Exception(res.errorBody()?.string() ?: "getMe failed"))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateMe(token: String, payload: Map<String, String>): Result<Unit> = try {
        val res = api.updateMe(payload)
        if (res.isSuccessful) Result.success(Unit)
        else Result.failure(Exception(res.errorBody()?.string() ?: "updateMe failed"))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun checkUsernameAvailable(username: String): Result<Boolean> = try {
        val res = api.checkUsernameAvailability(username)
        when {
            res.isSuccessful -> Result.success(true)
            res.code() == 409 -> Result.success(false)
            else -> Result.failure(Exception("checkUsername: ${res.code()}"))
        }
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun login(username: String, password: String): Result<LoginResponse> = try {
        val res = api.loginUser(LoginRequest(username, password))
        if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
        else Result.failure(Exception(res.errorBody()?.string() ?: "login failed"))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun changePassword(
        token: String,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Result<Unit> = try {
        val res = api.changePassword(
            mapOf(
                "currentPassword" to currentPassword,
                "newPassword" to newPassword,
                "confirmNewPassword" to confirmNewPassword
            )
        )
        if (res.isSuccessful) Result.success(Unit)
        else Result.failure(Exception(res.errorBody()?.string() ?: "changePassword failed"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
