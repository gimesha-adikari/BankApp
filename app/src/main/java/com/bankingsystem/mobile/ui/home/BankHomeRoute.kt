package com.bankingsystem.mobile.ui.home

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bankingsystem.mobile.data.local.DefaultAccountStore
import com.bankingsystem.mobile.data.repository.AccountRepository

@Composable
fun BankHomeRoute(
    userName: String,
    selectedItem: String = "Home",
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: HomeViewModel = viewModel(
        factory = HomeVMFactory(
            repo = AccountRepository(),
            defaults = DefaultAccountStore(context)
        )
    )
    val ui by vm.ui.collectAsState()

    BankHomeScreen(
        userName = userName,
        balance = ui.balance,
        selectedItem = selectedItem,
        onNavigate = onNavigate
    )
}
