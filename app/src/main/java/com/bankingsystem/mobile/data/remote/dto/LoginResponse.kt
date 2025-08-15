package com.bankingsystem.mobile.data.remote.dto

data class LoginResponse(
    val token: String,
    val username: String,
    val role: String
)