package com.bankingsystem.mobile.ui.kyc

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/* ---------------- Shared “glass” panel to match Home/Accounts ---------------- */

@Composable
private fun KycPanel(content: @Composable ColumnScope.() -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = cs.surface.copy(alpha = 0.10f),
        tonalElevation = 0.dp,
        shadowElevation = 12.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

/* ---------------- Step header chips (moved here to fix unresolved refs) ---------------- */

@Composable
private fun StepHeaderRow(current: KycStep, modifier: Modifier = Modifier) {
    val steps = listOf(
        Triple(Icons.Filled.CreditCard, "ID", KycStep.Document),
        Triple(Icons.Filled.Face,       "Selfie", KycStep.Selfie),
        Triple(Icons.Filled.Home,       "Address", KycStep.Address),
        Triple(Icons.Filled.CheckCircle,"", KycStep.Review)
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEach { (icon, label, step) ->
            val active = step == current
            AssistChip(
                onClick = { /* indicator only */ },
                label = { Text(label, color = if (active) MaterialTheme.colorScheme.primary else Color.White.copy(0.7f)) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (active) MaterialTheme.colorScheme.primary else Color.White.copy(0.7f)
                    )
                },
                border = BorderStroke(1.dp,
                    (if (active) MaterialTheme.colorScheme.primary else Color.White).copy(alpha = 0.45f)
                ),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.White.copy(alpha = if (active) 0.10f else 0.06f)
                )
            )
        }
    }
}

/* ---------------- 1) DOCUMENT CAPTURE ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentCaptureScreen(
    frontUri: Uri?,
    backUri: Uri?,
    onPickFront: () -> Unit,
    onRemoveFront: () -> Unit,
    onPickBack: () -> Unit,
    onRemoveBack: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    canContinue: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = { Text("Verify your ID", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StepHeaderRow(current = KycStep.Document)

            KycPanel {
                Text("Capture both sides of your national ID.", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DocSlot("Front side", frontUri, onPickFront, onRemoveFront, Modifier.weight(1f))
                    DocSlot("Back side",  backUri,  onPickBack,  onRemoveBack,  Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
                    Button(onClick = onNext, enabled = canContinue, modifier = Modifier.weight(1f)) {
                        Text("Continue")
                    }
                }
            }
        }
    }
}

@Composable
private fun DocSlot(
    title: String,
    uri: Uri?,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grad = remember {
        Brush.linearGradient(
            listOf(
                Color(0xFF5B67F3).copy(alpha = 0.22f),
                Color(0xFF5B67F3).copy(alpha = 0.08f)
            )
        )
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .background(grad, RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)

            if (uri == null) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(0.2f))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TextButton(onClick = onPick) {
                            Icon(Icons.Filled.AddAPhoto, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp)); Text("Add photo", color = Color.White)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) { Text("Photo attached", color = Color.White) }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onRemove) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp)); Text("Remove", color = Color.White)
                    }
                }
            }
        }
    }
}

/* ---------------- 2) SELFIE / LIVENESS ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfieLivenessScreen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    canContinue: Boolean = true
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = { Text("Face verification", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StepHeaderRow(current = KycStep.Selfie)

            KycPanel {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.06f)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Camera preview / liveness here", color = Color.White)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Button(onClick = onNext, enabled = canContinue, modifier = Modifier.fillMaxWidth()) {
                    Text("Continue")
                }
            }
        }
    }
}

/* ---------------- 3) ADDRESS PROOF ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressProofScreen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    canContinue: Boolean = true
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = { Text("Proof of address", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StepHeaderRow(current = KycStep.Address)

            KycPanel {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.06f)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Upload / capture utility bill, lease, etc.", color = Color.White)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Button(onClick = onNext, enabled = canContinue, modifier = Modifier.fillMaxWidth()) {
                    Text("Continue")
                }
            }
        }
    }
}

/* ---------------- 4) REVIEW & SUBMIT ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycReviewScreen(
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    canSubmit: Boolean = true
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = { Text("Review & submit", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StepHeaderRow(current = KycStep.Review)

            KycPanel {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("KYC summary", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF34D399))
                }
                Spacer(Modifier.height(8.dp))
                Text("Show a summary of extracted fields & photos here.", color = Color.White)

                Spacer(Modifier.height(12.dp))
                Button(onClick = onSubmit, enabled = canSubmit, modifier = Modifier.fillMaxWidth()) {
                    Text("Submit")
                }
            }
        }
    }
}
