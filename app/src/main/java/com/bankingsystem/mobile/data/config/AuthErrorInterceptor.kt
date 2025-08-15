package com.bankingsystem.mobile.data.config

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Central place to react to 401s (e.g., clear session, trigger logout navigation).
 * Wire a callback from your Application/DI layer if you want auto-logout.
 */
class AuthErrorInterceptor(
    private val onUnauthorized: () -> Unit = {}
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            onUnauthorized()
        }
        return response
    }
}
