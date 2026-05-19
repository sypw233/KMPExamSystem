package ovo.sypw.kmp.examsystem.presentation.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ovo.sypw.kmp.examsystem.utils.LocalResponsiveConfig
import ovo.sypw.kmp.examsystem.utils.ResponsiveUtils

@Composable
fun adaptiveDialogModifier(
    maxWidth: Dp? = null,
    maxHeight: Dp? = null
): Modifier {
    val config = LocalResponsiveConfig.current
    val horizontalInset = 10.dp
    val widthLimit = maxWidth ?: when (config.screenSize) {
        ResponsiveUtils.ScreenSize.COMPACT -> Dp.Unspecified
        ResponsiveUtils.ScreenSize.MEDIUM -> 520.dp
        ResponsiveUtils.ScreenSize.EXPANDED -> 600.dp
    }
    val heightLimit = maxHeight ?: when (config.screenSize) {
        ResponsiveUtils.ScreenSize.COMPACT -> 720.dp
        ResponsiveUtils.ScreenSize.MEDIUM -> 760.dp
        ResponsiveUtils.ScreenSize.EXPANDED -> 820.dp
    }
    return when (config.screenSize) {
        ResponsiveUtils.ScreenSize.COMPACT -> Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalInset)
            .heightIn(max = heightLimit)

        ResponsiveUtils.ScreenSize.MEDIUM -> Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalInset)
            .widthIn(max = widthLimit)
            .heightIn(max = heightLimit)

        ResponsiveUtils.ScreenSize.EXPANDED -> Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalInset)
            .widthIn(max = widthLimit)
            .heightIn(max = heightLimit)
    }
}

fun adaptiveDialogProperties(): DialogProperties =
    DialogProperties(usePlatformDefaultWidth = false)
