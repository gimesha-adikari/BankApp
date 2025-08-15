package com.bankingsystem.mobile.ui.profile

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bankingsystem.mobile.data.local.AuthStore
import com.bankingsystem.mobile.data.remote.AuthApi

@Composable
fun ProfileRoute(
    api: AuthApi,
    store: AuthStore,
    vm: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(api, store)),
    onNavigate: (String) -> Unit = {}
) {
    val ui by vm.ui.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ui.snackbarMessage) {
        ui.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.snackbarShown()
        }
    }

    ProfileScreen(
        profile = ui.profile,
        isLoading = ui.isLoading,
        errorMessage = ui.errorMessage,

        editingField = ui.editingField,

        tempValue = ui.tempValue,

        onEditClicked = vm::startEditing,
        onCancelEditing = vm::cancelEditing,
        onSaveEditing = vm::saveEditing,
        onValueChange = vm::onTempValueChange,
        onChangePasswordClick = vm::openChangePassword,

        selectedItem = "Profile",
        onNavigate = onNavigate,

        isCheckingUsername = ui.isCheckingUsername,
        isUsernameAvailable = ui.isUsernameAvailable
    )

    if (ui.showPasswordDialog) {
        PasswordVerificationDialog(
            onConfirm = { vm.onPasswordVerified(it) },
            onDismiss = { vm.dismissPasswordDialog() }
        )
    }

    if (ui.showChangePasswordDialog) {
        ChangePasswordDialog(
            onConfirm = { current, newPass, confirm ->
                vm.changePassword(current, newPass, confirm)
            },
            onDismiss = { vm.dismissChangePassword() }
        )
    }

    SnackbarHost(hostState = snackbarHostState)
}
