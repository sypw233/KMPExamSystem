package ovo.sypw.kmp.examsystem.presentation.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@Composable
fun adaptiveDialogModifier(): Modifier {
    val config = LocalResponsiveConfig.current
    val horizontalInset = 24.dp
    return when (config.screenSize) {
        ResponsiveUtils.ScreenSize.COMPACT -> Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalInset)

        ResponsiveUtils.ScreenSize.MEDIUM -> Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalInset)
            .widthIn(max = 600.dp)

        ResponsiveUtils.ScreenSize.EXPANDED -> Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalInset)
            .widthIn(max = 680.dp)
    }
}

fun adaptiveDialogProperties(): DialogProperties =
    DialogProperties(usePlatformDefaultWidth = false)
