package com.bankingsystem.mobile.ui.kyc

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bankingsystem.mobile.data.model.kyc.KycCheckDto
import com.bankingsystem.mobile.ui.components.FadingAppBackground
import com.bankingsystem.mobile.ui.components.Sidebar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun KycStatusRoute(
    userName: String,
    selectedItem: String = "KYC Status",
    onNavigate: (String) -> Unit = {}
) {
    val vm: KycViewModel = hiltViewModel()
    val status by vm.status.collectAsState()
    val checks by vm.checks.collectAsState()

    DisposableEffect(Unit) {
        vm.startPollingStatus()
        onDispose { vm.stopPollingStatus() }
    }

    val cs = MaterialTheme.colorScheme
    var isSidebarOpen by remember { mutableStateOf(false) }
    val sidebarOffset by animateDpAsState(
        targetValue = if (isSidebarOpen) 0.dp else (-300).dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "sidebar"
    )
    val scrimAlpha by animateFloatAsState(if (isSidebarOpen) 0.5f else 0f, label = "scrim")

    Box(Modifier.fillMaxSize()) {
        FadingAppBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    title = { Text("KYC Status", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { isSidebarOpen = !isSidebarOpen }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open navigation", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                // Status panel (glass/outlined look like other screens)
                KycPanel {
                    val st = status?.status ?: "—"
                    val terminal = st in setOf("APPROVED", "REJECTED", "NEEDS_MORE_INFO")
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current status", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        StatusPill(text = st)
                    }
                    if (!terminal) {
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
                        Text("We’re reviewing your documents. You can keep using the app.",
                            color = Color.White.copy(alpha = 0.85f))
                    }
                    if (!status?.decisionReason.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Reason: ${status?.decisionReason}", color = Color.White)
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Checks list
                KycPanel {
                    Text("Checks", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 0.dp, max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(checks) { c -> KycCheckItemGlass(check = c) }
                    }
                }
            }
        }

        if (scrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.scrim.copy(alpha = scrimAlpha))
                    .clickable { isSidebarOpen = false }
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .offset(x = sidebarOffset),
            tonalElevation = 0.dp,
            shadowElevation = 16.dp,
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        ) {
            Sidebar(
                selectedItem = selectedItem,
                onItemClick = {
                    isSidebarOpen = false
                    onNavigate(it)
                },
                userName = userName
            )
        }
    }
}

/* ---------- helpers ---------- */

@Composable
private fun StatusPill(text: String) {
    val cs = MaterialTheme.colorScheme
    val bg = when (text.uppercase()) {
        "APPROVED" -> Color(0xFF065F46).copy(alpha = 0.35f)
        "REJECTED" -> Color(0xFF7F1D1D).copy(alpha = 0.35f)
        "NEEDS_MORE_INFO" -> Color(0xFF92400E).copy(alpha = 0.35f)
        else -> cs.primary.copy(alpha = 0.18f)
    }
    val fg = when (text.uppercase()) {
        "APPROVED" -> Color(0xFF34D399)
        "REJECTED" -> Color(0xFFFCA5A5)
        "NEEDS_MORE_INFO" -> Color(0xFFFBBF24)
        else -> Color.White
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun KycCheckItemGlass(check: KycCheckDto) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.06f),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, cs.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(check.type, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                val score = check.score?.let { String.format("%.2f", it) } ?: "—"
                val passed = when (check.passed) { true -> "true"; false -> "false"; null -> "—" }
                Text("score=$score • passed=$passed",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    Spacer(Modifier.height(4.dp))
}
