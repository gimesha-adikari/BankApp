package com.bankingsystem.mobile.data.remote

import com.bankingsystem.mobile.data.model.UserProfile
import com.bankingsystem.mobile.data.remote.dto.LoginResponse

interface AuthApi {
    suspend fun getMe(token: String): Result<UserProfile>
    suspend fun updateMe(token: String, payload: Map<String, String>): Result<Unit>
    suspend fun checkUsernameAvailable(username: String): Result<Boolean>
    suspend fun login(username: String, password: String): Result<LoginResponse>
    suspend fun changePassword(token: String, currentPassword: String, newPassword: String,confirmNewPassword: String): Result<Unit>

}
