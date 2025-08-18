package com.bankingsystem.mobile.ui.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val PAYMENTS = "payments"
    const val SETTINGS = "settings"

    const val ACCOUNTS_MY = "accounts/my"
    const val ACCOUNTS_OPEN = "accounts/open"
    const val ACCOUNT_TX    = "accounts/tx/{accountId}?accNo={accNo}"

    const val KYC = "kyc"

    fun accountTx(accountId: String, accountNumber: String? = null): String {
        val accNo = accountNumber?.let { Uri.encode(it) } ?: ""
        return "accounts/tx/$accountId?accNo=$accNo"
    }
}
