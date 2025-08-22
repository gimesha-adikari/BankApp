package com.bankingsystem.mobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bankingsystem.mobile.ui.account.AccountTransactionsRoute
import com.bankingsystem.mobile.ui.account.AccountsRoute
import com.bankingsystem.mobile.ui.account.MyAccountsRoute
import com.bankingsystem.mobile.ui.home.BankHomeRoute
import com.bankingsystem.mobile.ui.home.BankHomeScreen
import com.bankingsystem.mobile.ui.kyc.KycRoute
import com.bankingsystem.mobile.ui.kyc.KycStatusRoute
import com.bankingsystem.mobile.ui.profile.ProfileRoute
import com.bankingsystem.mobile.ui.settings.SettingsScreen

@ExperimentalMaterial3Api
@Composable
fun AppNavHost(
    userName: String,
    onLogout: () -> Unit,
    nav: NavHostController = rememberNavController()
) {
    NavHost(navController = nav, startDestination = Routes.HOME) {

        /* ---------- Home ---------- */
        composable(Routes.HOME) {
            BankHomeRoute(
                userName = userName,
                selectedItem = "Home",
                onNavigate = { label -> navigateByLabel(nav, label, onLogout) }
            )
        }

        /* ---------- Profile ---------- */
        composable(Routes.PROFILE) {
            // Hilt-backed screen; no manual api/store
            ProfileRoute(
                onNavigate = { label -> navigateByLabel(nav, label, onLogout) }
            )
        }

        /* ---------- Settings ---------- */
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onLogout = onLogout,
                onBack = { nav.navigateUp() }
            )
        }

        /* ---------- Accounts: My list ---------- */
        composable(Routes.ACCOUNTS_MY) {
            MyAccountsRoute(
                userName = userName,
                onNavigate = { label -> navigateByLabel(nav, label, onLogout) }
            )
        }

        /* ---------- Accounts: Open account ---------- */
        composable(Routes.ACCOUNTS_OPEN) {
            AccountsRoute(
                userName = userName,
                onNavigate = { label -> navigateByLabel(nav, label, onLogout) }
            )
        }

        /* ---------- Accounts: Transactions ---------- */
        composable(
            route = Routes.ACCOUNT_TX,
            arguments = listOf(
                navArgument("accountId") { type = NavType.StringType },
                navArgument("accNo") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStack ->
            val id = backStack.arguments?.getString("accountId").orEmpty()
            val accNo = backStack.arguments?.getString("accNo").orEmpty()

            AccountTransactionsRoute(
                userName = userName,
                accountId = id,
                accountNumber = accNo.ifBlank { null },
                onNavigate = { label -> navigateByLabel(nav, label, onLogout) },
                onBack = { nav.navigateUp() }
            )
        }

        /* ---------- KYC ---------- */
        composable(Routes.KYC) {
            KycRoute(
                userName = userName,
                onNavigate = { label -> navigateByLabel(nav, label, onLogout) }
            )
        }

        /* ---------- KYC STATUS---------- */
        composable(Routes.KYC_STATUS) {
            KycStatusRoute(
                userName = userName,
                selectedItem = "KYC Status",
                onNavigate = { label -> navigateByLabel(nav, label, onLogout) },
            )
        }


        /* ---------- Payments (placeholder) ---------- */
        composable(Routes.PAYMENTS) {
            BankHomeScreen(
                userName = userName,
                selectedItem = "Payments",
                onNavigate = { label -> navigateByLabel(nav, label, onLogout) }
            )
        }
    }
}

/* ---------------- helpers ---------------- */

private fun navigateByLabel(
    nav: NavHostController,
    label: String,
    onLogout: () -> Unit
) {
    if (label.contains('/')) {
        if (label.startsWith("accounts/tx/")) {
            nav.navigate(label)
        } else {
            nav.navigate(label) {
                popUpTo(nav.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        return
    }

    val route = when (label) {
        "Home" -> Routes.HOME
        "Profile" -> Routes.PROFILE
        "Settings" -> Routes.SETTINGS
        "Payments" -> Routes.PAYMENTS
        "My Accounts" -> Routes.ACCOUNTS_MY
        "Open Account" -> Routes.ACCOUNTS_OPEN
        "Verify Identity", Routes.KYC -> Routes.KYC
        "KYC Status" -> Routes.KYC_STATUS
        "Logout" -> { onLogout(); return }
        else -> return
    }

    nav.navigate(route) {
        popUpTo(nav.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
