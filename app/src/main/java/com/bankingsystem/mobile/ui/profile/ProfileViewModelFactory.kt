package com.bankingsystem.mobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bankingsystem.mobile.data.local.AuthStore
import com.bankingsystem.mobile.data.remote.AuthApi

class ProfileViewModelFactory(
    private val api: AuthApi,
    private val store: AuthStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(api, store) as T
    }
}
