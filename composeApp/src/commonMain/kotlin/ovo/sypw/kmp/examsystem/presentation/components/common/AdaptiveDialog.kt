package ovo.sypw.kmp.examsystem.presentation.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@Composable
fun adaptiveDialogModifier(): Modifier {
    val config = LocalResponsiveConfig.current
    val horizontalInset = with(LocalDensity.current) { 10.toDp() }
    return if (config.screenSize == ResponsiveUtils.ScreenSize.COMPACT) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalInset)
    } else {
        Modifier.fillMaxWidth()
    }
}

fun adaptiveDialogProperties(): DialogProperties =
    DialogProperties(usePlatformDefaultWidth = false)
