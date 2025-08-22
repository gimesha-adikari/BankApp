package com.bankingsystem.mobile

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bankingsystem.mobile.data.storage.LockPreferences
import com.bankingsystem.mobile.ui.locker.AppLocker
import com.bankingsystem.mobile.ui.login.LoginScreen
import com.bankingsystem.mobile.ui.login.LoginState
import com.bankingsystem.mobile.ui.login.LoginViewModel
import com.bankingsystem.mobile.ui.navigation.AppNavHost
import com.bankingsystem.mobile.ui.register.RegisterScreen
import com.bankingsystem.mobile.ui.theme.BankAppTheme
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalMaterial3Api
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var lockPreferences: LockPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        lockPreferences = LockPreferences(this)

        setContent {
            BankAppTheme {
                val loginViewModel: LoginViewModel = hiltViewModel()
                val loginState by loginViewModel.loginState.collectAsState()

                var lockerAuthenticated by remember { mutableStateOf(false) }
                var lockEnabled by remember { mutableStateOf(false) }
                var storedPin by remember { mutableStateOf("") }
                var showRegister by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) { loginViewModel.autoLogin() }

                LaunchedEffect(loginState) {
                    if (loginState is LoginState.Success) {
                        lockEnabled = lockPreferences.isLockEnabled()
                        storedPin = lockPreferences.getPin() ?: ""
                        lockerAuthenticated = !lockEnabled
                        showRegister = false
                    } else {
                        lockerAuthenticated = false
                        lockEnabled = false
                        storedPin = ""
                    }
                }

                val doLogout: () -> Unit = {
                    lockerAuthenticated = false
                    lockEnabled = false
                    storedPin = ""
                    loginViewModel.logout()
                }

                when (val state = loginState) {
                    is LoginState.Success -> {
                        if (lockEnabled && !lockerAuthenticated) {
                            AppLocker(
                                correctPin = storedPin,
                                onAuthenticated = { lockerAuthenticated = true }
                            )
                        } else {
                            AppNavHost(
                                userName = state.username,
                                onLogout = doLogout
                            )
                        }
                    }
                    is LoginState.Error, LoginState.Idle, is LoginState.Loading -> {
                        if (showRegister) {
                            RegisterScreen(
                                onRegisterSuccess = { showRegister = false },
                                onNavigateToLogin = { showRegister = false }
                            )
                        } else {
                            LoginScreen(
                                onNavigate = { },
                                onNavigateToRegister = { showRegister = true }
                            )
                        }
                    }
                }
            }
        }
    }
}
