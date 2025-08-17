package com.bankingsystem.mobile.data.local

import com.bankingsystem.mobile.data.storage.TokenManager

class AuthStoreImpl(
    private val tokenManager: TokenManager
) : AuthStore {
    override suspend fun getToken(): String? = tokenManager.getToken()

    override suspend fun saveAuth(token: String, username: String, role: String) {
        tokenManager.saveToken(token)
    }
}
