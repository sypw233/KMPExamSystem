package ovo.sypw.kmp.examsystem.domain

/**
 * 弹窗配置
 * @param type 弹窗类型
 * @param title 标题
 * @param message 消息内容
 * @param confirmText 确认按钮文本
 * @param cancelText 取消按钮文本，null 表示只显示确认按钮
 * @param onConfirm 确认回调
 * @param onCancel 取消回调
 * @param dismissOnBackPress 按返回键是否关闭弹窗
 * @param dismissOnClickOutside 点击外部是否关闭弹窗
 */
data class DialogConfig(
    val type: DialogType = DialogType.INFO,
    val title: String,
    val message: String,
    val confirmText: String = "确定",
    val cancelText: String? = null,
    val onConfirm: (() -> Unit)? = null,
    val onCancel: (() -> Unit)? = null,
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true
)
