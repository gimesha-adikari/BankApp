package com.bankingsystem.mobile.ui.account

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel  // <-- use Hilt VM
import com.bankingsystem.mobile.ui.components.FadingAppBackground
import com.bankingsystem.mobile.ui.components.Sidebar
import com.bankingsystem.mobile.ui.navigation.Routes
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAccountsRoute(
    userName: String,
    onNavigate: (String) -> Unit
) {
    val vm: MyAccountsViewModel = hiltViewModel()
    val ui by vm.ui.collectAsState()

    val config = LocalConfiguration.current
    val isCompact = config.screenWidthDp < 600
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val topBar: @Composable () -> Unit = {
        TopAppBar(
            title = { Text("Accounts", color = Color.White) },
            navigationIcon = {
                if (isCompact) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                }
            },
            actions = {
                TextButton(onClick = vm::refresh, enabled = !ui.loading) {
                    Text(if (ui.loading) "Refreshing…" else "Refresh")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }

    val content: @Composable (PaddingValues) -> Unit = { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            MyAccountsScreen(
                accounts = ui.accounts,
                loading = ui.loading,
                error = ui.error,
                onRefresh = vm::refresh,
                onOpenAccount = { onNavigate(Routes.ACCOUNTS_OPEN) },
                onAccountClick = { id, number -> onNavigate(Routes.accountTx(id, number)) },
                defaultAccountId = ui.defaultAccountId,
                onSetDefault = vm::setDefault,
                customerMissing = ui.customerMissing,
                customerMessage = ui.customerMessage,
                onStartKyc = { onNavigate(Routes.KYC) },
                onFixCustomer = { onNavigate(Routes.PROFILE) }
            )
        }
    }

    if (isCompact) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = Color(0xFF0B0B12)) {
                    Sidebar(
                        selectedItem = "My Accounts",
                        onItemClick = { label ->
                            scope.launch { drawerState.close() }
                            onNavigate(label)
                        },
                        userName = userName
                    )
                }
            }
        ) {
            Box(Modifier.fillMaxSize()) {
                FadingAppBackground()
                Scaffold(containerColor = Color.Transparent, topBar = topBar, content = content)
            }
        }
    } else {
        Row(Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxHeight().width(300.dp),
                tonalElevation = 0.dp,
                shadowElevation = 16.dp
            ) {
                Sidebar(
                    selectedItem = "My Accounts",
                    onItemClick = onNavigate,
                    userName = userName
                )
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                FadingAppBackground()
                Scaffold(containerColor = Color.Transparent, topBar = topBar, content = content)
            }
        }
    }
}
