package ovo.sypw.kmp.examsystem.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ovo.sypw.kmp.examsystem.domain.DialogConfig
import ovo.sypw.kmp.examsystem.domain.DialogType

/**
 * 全局弹窗管理器
 * 管理应用中所有弹窗的显示和隐藏
 */
class DialogManager {
    
    private val _currentDialog = MutableStateFlow<DialogConfig?>(null)
    val currentDialog: StateFlow<DialogConfig?> = _currentDialog.asStateFlow()
    
    private val dialogQueue = mutableListOf<DialogConfig>()
    
    /**
     * 显示信息弹窗
     */
    fun showInfo(
        title: String,
        message: String,
        confirmText: String = "确定",
        onConfirm: (() -> Unit)? = null
    ) {
        show(
            DialogConfig(
                type = DialogType.INFO,
                title = title,
                message = message,
                confirmText = confirmText,
                onConfirm = onConfirm
            )
        )
    }
    
    /**
     * 显示警告弹窗
     */
    fun showWarning(
        title: String,
        message: String,
        confirmText: String = "我知道了",
        onConfirm: (() -> Unit)? = null
    ) {
        show(
            DialogConfig(
                type = DialogType.WARNING,
                title = title,
                message = message,
                confirmText = confirmText,
                onConfirm = onConfirm
            )
        )
    }
    
    /**
     * 显示错误弹窗
     */
    fun showError(
        title: String = "错误",
        message: String,
        confirmText: String = "确定",
        onConfirm: (() -> Unit)? = null
    ) {
        show(
            DialogConfig(
                type = DialogType.ERROR,
                title = title,
                message = message,
                confirmText = confirmText,
                onConfirm = onConfirm
            )
        )
    }
    
    /**
     * 显示成功弹窗
     */
    fun showSuccess(
        title: String = "成功",
        message: String,
        confirmText: String = "确定",
        onConfirm: (() -> Unit)? = null
    ) {
        show(
            DialogConfig(
                type = DialogType.SUCCESS,
                title = title,
                message = message,
                confirmText = confirmText,
                onConfirm = onConfirm
            )
        )
    }
    
    /**
     * 显示确认弹窗
     */
    fun showConfirm(
        title: String,
        message: String,
        confirmText: String = "确定",
        cancelText: String = "取消",
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        show(
            DialogConfig(
                type = DialogType.CONFIRM,
                title = title,
                message = message,
                confirmText = confirmText,
                cancelText = cancelText,
                onConfirm = onConfirm,
                onCancel = onCancel
            )
        )
    }
    
    /**
     * 显示自定义弹窗
     */
    fun show(config: DialogConfig) {
        if (_currentDialog.value == null) {
            _currentDialog.value = config
        } else {
            dialogQueue.add(config)
        }
    }
    
    /**
     * 关闭当前弹窗
     */
    fun dismiss() {
        _currentDialog.value = null
        showNextDialog()
    }
    
    /**
     * 显示队列中的下一个弹窗
     */
    private fun showNextDialog() {
        if (dialogQueue.isNotEmpty()) {
            _currentDialog.value = dialogQueue.removeAt(0)
        }
    }
    
    /**
     * 清空所有弹窗
     */
    fun clearAll() {
        dialogQueue.clear()
        _currentDialog.value = null
    }
}
