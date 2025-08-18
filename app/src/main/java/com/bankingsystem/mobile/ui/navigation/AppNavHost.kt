package com.bankingsystem.mobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bankingsystem.mobile.App
import com.bankingsystem.mobile.data.config.RetrofitClient
import com.bankingsystem.mobile.data.local.AuthStore
import com.bankingsystem.mobile.data.local.AuthStoreImpl
import com.bankingsystem.mobile.data.remote.AuthApiImpl
import com.bankingsystem.mobile.data.storage.TokenManager
import com.bankingsystem.mobile.ui.account.AccountTransactionsRoute
import com.bankingsystem.mobile.ui.account.AccountsRoute
import com.bankingsystem.mobile.ui.account.MyAccountsRoute
import com.bankingsystem.mobile.ui.home.BankHomeRoute
import com.bankingsystem.mobile.ui.home.BankHomeScreen
import com.bankingsystem.mobile.ui.kyc.KycRoute
import com.bankingsystem.mobile.ui.profile.ProfileRoute
import com.bankingsystem.mobile.ui.settings.SettingsScreen

@Composable
fun AppNavHost(
    userName: String,
    onLogout: () -> Unit,
    nav: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as? App
    val authStore: AuthStore = app?.authStore ?: remember { AuthStoreImpl(TokenManager(context)) }

    var retrofitReady by remember { mutableStateOf(RetrofitClient.isInitialized()) }
    LaunchedEffect(authStore) {
        if (!retrofitReady) {
            RetrofitClient.init(authStore = authStore)
            retrofitReady = true
        }
    }

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
            if (!retrofitReady) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val apiService = remember { RetrofitClient.apiService }
                val authApi = remember { AuthApiImpl(apiService) }
                ProfileRoute(
                    api = authApi,
                    store = authStore,
                    onNavigate = { label -> navigateByLabel(nav, label, onLogout) }
                )
            }
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
                navArgument("accNo") {
                    type = NavType.StringType
                    defaultValue = ""       // optional
                }
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

        composable(Routes.KYC) {
            KycRoute(
                userName = userName,
                onNavigate = { label -> navigateByLabel(nav, label, onLogout) }
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
        "Verify Identity",
        Routes.KYC
            -> Routes.KYC

        "Logout" -> {
            onLogout(); return
        }

        else -> return
    }

    nav.navigate(route) {
        popUpTo(nav.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
