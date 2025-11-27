package ovo.sypw.kmp.examsystem.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kmpexamsystem.composeapp.generated.resources.Mac方正小标宋简体
import kmpexamsystem.composeapp.generated.resources.Res

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

/**
 * 字体工具类
 * 提供自定义字体加载功能
 */
object FontUtils {

    /**
     * 加载内容字体
     * @return 自定义字体族
     */
    @OptIn(ExperimentalResourceApi::class)
    @Composable
    fun loadContentFont(): FontFamily {
        val customFont = FontFamily(
            Font(
                resource = Res.font.Mac方正小标宋简体,
                weight = FontWeight.Normal
            )
        )
        return customFont
    }

    /**
     * 获取默认字体族
     * @return 包含自定义字体的字体族
     */
    @Composable
    fun getDefaultFontFamily(): FontFamily {
        return loadContentFont()
    }
}