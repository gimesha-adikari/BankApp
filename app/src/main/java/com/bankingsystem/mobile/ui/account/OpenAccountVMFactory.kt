package com.bankingsystem.mobile.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bankingsystem.mobile.data.config.RetrofitClient
import com.bankingsystem.mobile.data.repository.AccountRepository

class OpenAccountVMFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val api = RetrofitClient.apiService
        val repo = AccountRepository(api)
        return OpenAccountViewModel(repo) as T
    }
}
