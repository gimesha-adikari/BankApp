package com.bankingsystem.mobile.ui.account

import com.bankingsystem.mobile.data.model.account.AccountNet
import com.bankingsystem.mobile.data.model.account.AccountOpenRequest
import com.bankingsystem.mobile.ui.account.models.AccountStatus
import com.bankingsystem.mobile.ui.account.models.AccountType
import com.bankingsystem.mobile.ui.account.models.AccountUi

fun toOpenRequest(
    type: AccountType,
    initialDeposit: String,
    branchId: String
): AccountOpenRequest {
    val amount = initialDeposit.toDoubleOrNull() ?: 0.0
    return AccountOpenRequest(
        accountType = type.name,
        initialDeposit = amount,
        branchId = branchId
    )
}

private fun String.toAccountType(): AccountType = when (uppercase()) {
    "SAVINGS"        -> AccountType.SAVINGS
    "CHECKING"       -> AccountType.CHECKING
    "FIXED_DEPOSIT"  -> AccountType.FIXED_DEPOSIT
    else             -> AccountType.SAVINGS
}

private fun String.toAccountStatus(): AccountStatus = when (uppercase()) {
    "ACTIVE" -> AccountStatus.ACTIVE
    "FROZEN" -> AccountStatus.FROZEN
    "CLOSED" -> AccountStatus.CLOSED
    else     -> AccountStatus.ACTIVE
}

fun AccountNet.toUi(): AccountUi = AccountUi(
    accountId      = accountId,
    accountNumber  = accountNumber,
    type           = accountType.toAccountType(),
    status         = accountStatus.toAccountStatus(),
    balance        = balance,
    branchName     = branchName
)
