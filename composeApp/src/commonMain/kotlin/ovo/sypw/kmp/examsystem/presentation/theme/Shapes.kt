package ovo.sypw.kmp.examsystem.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 自定义 M3 Shapes 定义
 * 统一的圆角半径令牌
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(20.dp),  // OutlinedTextField default shape
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
