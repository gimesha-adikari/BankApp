package com.bankingsystem.mobile.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bankingsystem.mobile.data.local.DefaultAccountStore
import com.bankingsystem.mobile.data.repository.AccountRepository
import com.bankingsystem.mobile.data.repository.CustomerMissingException
import com.bankingsystem.mobile.ui.account.models.AccountUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MyAccountsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val accounts: List<AccountUi> = emptyList(),
    val defaultAccountId: String? = null,
    val customerMissing: Boolean = false,
    val customerMessage: String? = null
)

@HiltViewModel
class MyAccountsViewModel @Inject constructor(
    private val repo: AccountRepository,
    private val defaults: DefaultAccountStore
) : ViewModel() {

    private val _ui = MutableStateFlow(MyAccountsUiState())
    val ui: StateFlow<MyAccountsUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            defaults.defaultAccountId.collect { id ->
                _ui.update { it.copy(defaultAccountId = id) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, customerMissing = false, customerMessage = null) }
            try {
                val list = repo.getMyAccounts().map { it.toUi() }
                _ui.update { it.copy(loading = false, accounts = list) }
            } catch (e: CustomerMissingException) {
                _ui.update { it.copy(loading = false, customerMissing = true, customerMessage = e.message) }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = e.message ?: "Failed to load accounts") }
            }
        }
    }

    fun setDefault(id: String) {
        viewModelScope.launch { defaults.setDefaultAccountId(id) }
    }
}
