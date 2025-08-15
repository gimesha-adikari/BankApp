package com.bankingsystem.mobile.data.local

interface AuthStore {
    suspend fun getToken(): String?
    suspend fun saveAuth(token: String, username: String, role: String)
}
