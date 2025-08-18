package com.bankingsystem.mobile.ui.kyc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bankingsystem.mobile.ui.components.FadingAppBackground

/** Toggle to false later to enforce validation before moving ahead. */
private const val DEV_PREVIEW = true

@Composable
fun KycRoute(
    userName: String,
    onNavigate: (String) -> Unit = {}
) {
    val vm: KycViewModel = viewModel()

    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        FadingAppBackground()

        val ui by vm.ui.collectAsState()

        when (ui.step) {
            KycStep.Document -> DocumentCaptureScreen(
                frontUri = ui.docFront,
                backUri = ui.docBack,
                onPickFront = { /* picker -> vm.setDocFront(uri) */ },
                onRemoveFront = { vm.setDocFront(null) },
                onPickBack = { /* picker -> vm.setDocBack(uri) */ },
                onRemoveBack = { vm.setDocBack(null) },
                onBack = { onNavigate("Home") },
                onNext = {
                    if (DEV_PREVIEW || vm.canContinueFromDocument()) vm.next()
                },
                canContinue = DEV_PREVIEW || (ui.docFront != null && ui.docBack != null)
            )

            KycStep.Selfie -> SelfieLivenessScreen(
                onBack = { vm.back() },
                onNext = {
                    if (DEV_PREVIEW || vm.canContinueFromSelfie()) vm.next()
                },
                canContinue = DEV_PREVIEW || vm.canContinueFromSelfie()
            )

            KycStep.Address -> AddressProofScreen(
                onBack = { vm.back() },
                onNext = {
                    if (DEV_PREVIEW || vm.canContinueFromAddress()) vm.next()
                },
                canContinue = DEV_PREVIEW || vm.canContinueFromAddress()
            )

            KycStep.Review -> KycReviewScreen(
                onBack = { vm.back() },
                onSubmit = {
                    if (DEV_PREVIEW || vm.canSubmit()) onNavigate("Home")
                },
                canSubmit = DEV_PREVIEW || vm.canSubmit()
            )
        }
    }
}
