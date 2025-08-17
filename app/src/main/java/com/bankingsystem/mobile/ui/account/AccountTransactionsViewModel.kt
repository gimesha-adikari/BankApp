package com.bankingsystem.mobile.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bankingsystem.mobile.data.model.account.TransactionNet
import com.bankingsystem.mobile.data.repository.AccountRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AccountTxUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val items: List<TransactionNet> = emptyList()
)

class AccountTransactionsViewModel(
    private val repo: AccountRepository
) : ViewModel() {

    companion object {
        fun factory(repo: AccountRepository = AccountRepository()): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { AccountTransactionsViewModel(repo) }
            }
    }

    private val _ui = MutableStateFlow(AccountTxUiState(loading = true))
    val ui: StateFlow<AccountTxUiState> = _ui.asStateFlow()

    private var currentAccountId: String? = null

    fun load(accountId: String) {
        if (currentAccountId == accountId && _ui.value.items.isNotEmpty()) return
        currentAccountId = accountId
        refresh()
    }

    fun refresh() {
        val id = currentAccountId ?: return
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val list = repo.getAccountTransactions(id)
                _ui.value = AccountTxUiState(loading = false, items = list)
            } catch (e: Exception) {
                _ui.value = AccountTxUiState(loading = false, error = e.message ?: "Failed to load transactions")
            }
        }
    }
}
