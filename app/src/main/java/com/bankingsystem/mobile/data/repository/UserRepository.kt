package com.bankingsystem.mobile.data.repository

import com.bankingsystem.mobile.data.model.RegisterRequest
import com.bankingsystem.mobile.data.model.ValidateTokenResponse
import com.bankingsystem.mobile.data.remote.dto.LoginRequest
import com.bankingsystem.mobile.data.remote.dto.LoginResponse
import com.bankingsystem.mobile.data.service.ApiService
import com.bankingsystem.mobile.data.storage.TokenManager
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.loginUser(LoginRequest(username, password))
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    tokenManager.saveToken(loginResponse.token)
                    Result.success(loginResponse)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Login failed: $errorMessage"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    suspend fun logout() {
        try {
            apiService.logout()
        } catch (_: Exception) {
        } finally {
            tokenManager.clearToken()
        }
    }

    suspend fun checkUsernameAvailability(username: String): Boolean {
        if (username.length < 3) return false
        return try {
            val response = apiService.checkUsernameAvailability(username)
            when {
                response.isSuccessful -> true
                response.code() == 409 -> false
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun registerUser(username: String, email: String, password: String): Boolean {
        return try {
            val request = RegisterRequest(username, email, password)
            val response = apiService.registerUser(request)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun forgotPassword(email: String): Boolean {
        return try {
            val response = apiService.forgotPassword(email)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun validateToken(): Result<ValidateTokenResponse> {
        return try {
            val response = apiService.validateToken()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Invalid or expired token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val tokenFlow = tokenManager.tokenFlow
}
