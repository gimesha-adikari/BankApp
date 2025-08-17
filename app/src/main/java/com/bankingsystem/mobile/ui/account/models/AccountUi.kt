package com.bankingsystem.mobile.ui.account.models

enum class AccountStatus { ACTIVE, FROZEN, CLOSED }
enum class AccountType { SAVINGS, CHECKING, FIXED_DEPOSIT }

data class AccountUi(
    val accountId: String,
    val accountNumber: String,
    val type: AccountType,
    val status: AccountStatus,
    val balance: Double,
    val branchName: String? = null
)
data class BranchOption(val id: String, val name: String)

val demoAccounts = listOf(
    AccountUi("a1","0123456789", AccountType.SAVINGS, AccountStatus.ACTIVE, 3500.0, "Colombo Main"),
    AccountUi("a2","9876543210", AccountType.CHECKING, AccountStatus.FROZEN, 120.45, "Kandy Central"),
    AccountUi("a3","1111222233", AccountType.FIXED_DEPOSIT, AccountStatus.CLOSED, 50000.0, "Galle Fort")
)
