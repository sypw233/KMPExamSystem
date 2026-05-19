package ovo.sypw.kmp.examsystem.presentation.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler

@Suppress("DEPRECATION")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    BackHandler(enabled = enabled, onBack = onBack)
}
