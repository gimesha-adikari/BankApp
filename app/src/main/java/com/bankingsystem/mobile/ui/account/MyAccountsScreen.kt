package com.bankingsystem.mobile.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bankingsystem.mobile.ui.account.models.AccountStatus
import com.bankingsystem.mobile.ui.account.models.AccountUi

@Composable
fun MyAccountsScreen(
    accounts: List<AccountUi>,
    loading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onOpenAccount: () -> Unit,
    onAccountClick: (String, String) -> Unit,
    defaultAccountId: String?,
    onSetDefault: (String) -> Unit,

    // 🔹 New: when backend says “customer not found”
    customerMissing: Boolean = false,
    customerMessage: String? = null,
    onStartKyc: () -> Unit = {},     // <— navigate to KYC
    onFixCustomer: () -> Unit = {},  // <— optionally go to Profile (kept for flexibility)

    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AccountBalance, null, tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text("My Accounts", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onRefresh, enabled = !loading) {
                Text(if (loading) "Refreshing…" else "Refresh")
            }
        }

        OpenAnotherAccountCTA(onOpenAccount = onOpenAccount)

        when {
            loading -> repeat(3) { AccountSkeleton() }
            error != null -> ErrorPanel(error, onRefresh)

            customerMissing -> CustomerMissingPanel(
                message = customerMessage ?: "We need to verify your identity and create your profile before you can view or open accounts.",
                onStartKyc = onStartKyc,
                onGoToProfile = onFixCustomer
            )

            accounts.isEmpty() -> EmptyAccounts(onOpenAccount)
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(accounts, key = { it.accountId }) { acc ->
                        AccountCard(
                            acc = acc,
                            isDefault = acc.accountId == defaultAccountId,
                            onSetDefault = { onSetDefault(acc.accountId) },
                            onClick = { onAccountClick(acc.accountId, acc.accountNumber) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorPanel(message: String, onRetry: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFB91C1C).copy(alpha = 0.18f)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Failed to load accounts", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(message.ifBlank { "Please try again." }, color = Color.White.copy(0.85f))
            Spacer(Modifier.height(10.dp))
            Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) { Text("Retry") }
        }
    }
}

@Composable
private fun OpenAnotherAccountCTA(onOpenAccount: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.08f),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Need another account?",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White, fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            FilledTonalButton(onClick = onOpenAccount, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Open Account")
            }
        }
    }
}

@Composable
private fun AccountCard(
    acc: AccountUi,
    isDefault: Boolean,
    onSetDefault: () -> Unit,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val gradient = Brush.linearGradient(
        listOf(Color(0xFF5B67F3).copy(alpha = 0.20f), Color(0xFF5B67F3).copy(alpha = 0.08f))
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = cs.surface.copy(alpha = 0.10f),
        tonalElevation = 0.dp,
        shadowElevation = 12.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(0.06f)) {
                    Text(
                        acc.type.name.replace('_',' ').lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(Modifier.width(8.dp))
                StatusPill(acc.status)

                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onSetDefault) {
                    Icon(
                        imageVector = if (isDefault) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (isDefault) "Default account" else "Set as default",
                        tint = if (isDefault) Color(0xFFFCD34D) else Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$" + "%,.2f".format(acc.balance),
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Text("Available balance", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.75f))
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 0.dp, color = Color.Transparent) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(gradient, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "•••• " + acc.accountNumber.takeLast(4),
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(12.dp))
                    acc.branchName?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: AccountStatus) {
    val (bg, text) = when (status) {
        AccountStatus.ACTIVE -> Color(0xFF064E3B) to Color(0xFF34D399)
        AccountStatus.FROZEN -> Color(0xFF1E293B) to Color(0xFF93C5FD)
        AccountStatus.CLOSED -> Color(0xFF3F1D1D) to Color(0xFFFCA5A5)
    }
    Surface(shape = RoundedCornerShape(10.dp), color = bg.copy(alpha = 0.5f)) {
        Text(
            status.name.lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable private fun AccountSkeleton() {
    Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.06f)) {
        Spacer(Modifier.fillMaxWidth().height(86.dp))
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun EmptyAccounts(onOpenAccount: () -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.06f)) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No accounts yet", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text("Open your first account to get started.", color = Color.White.copy(0.8f),
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Button(onClick = onOpenAccount, shape = RoundedCornerShape(12.dp)) { Text("Open Account") }
        }
    }
}

@Composable
private fun CustomerMissingPanel(
    message: String,
    onStartKyc: () -> Unit,
    onGoToProfile: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF7C3AED).copy(alpha = 0.18f)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Almost there!", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(message, color = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onStartKyc,
                shape = RoundedCornerShape(12.dp)
            ) { Text("Start Verification") }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onGoToProfile) { Text("Go to Profile") }
        }
    }
}
