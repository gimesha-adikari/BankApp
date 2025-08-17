package com.bankingsystem.mobile

import android.app.Application
import com.bankingsystem.mobile.data.config.RetrofitClient
import com.bankingsystem.mobile.data.local.AuthStore
import com.bankingsystem.mobile.data.local.AuthStoreImpl
import com.bankingsystem.mobile.data.storage.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    lateinit var tokenManager: TokenManager
        private set
    lateinit var authStore: AuthStore
        private set

    override fun onCreate() {
        super.onCreate()

        tokenManager = TokenManager(this)
        authStore = AuthStoreImpl(tokenManager)

        RetrofitClient.init(
            authStore = authStore,
            onUnauthorized = {
                CoroutineScope(Dispatchers.IO).launch {
                    try { tokenManager.clearToken() } catch (_: Exception) {}
                }
            }
        )
    }
}