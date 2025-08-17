package com.bankingsystem.mobile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bankingsystem.mobile.data.local.DefaultAccountStore
import com.bankingsystem.mobile.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val balance: Double = 0.0,
    val defaultAccountId: String? = null
)

class HomeViewModel(
    private val repo: AccountRepository,
    private val defaults: DefaultAccountStore
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            defaults.defaultAccountId.collect { id ->
                _ui.update { it.copy(defaultAccountId = id) }
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val accounts = repo.getMyAccounts()
                val pick = accounts.firstOrNull { it.accountId == _ui.value.defaultAccountId }
                    ?: accounts.firstOrNull()
                _ui.update { it.copy(loading = false, balance = pick?.balance ?: 0.0) }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }
}

class HomeVMFactory(
    private val repo: AccountRepository,
    private val defaults: DefaultAccountStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(repo, defaults) as T
}
