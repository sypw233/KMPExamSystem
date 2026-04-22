package ovo.sypw.kmp.examsystem.domain

/**
 * 弹窗类型
 */
enum class DialogType {
    /**
     * 信息弹窗 - 一般信息提示
     */
    INFO,
    
    /**
     * 警告弹窗 - 警告信息，需要用户注意
     */
    WARNING,
    
    /**
     * 错误弹窗 - 错误提示
     */
    ERROR,
    
    /**
     * 成功弹窗 - 操作成功提示
     */
    SUCCESS,
    
    /**
     * 确认弹窗 - 需要用户确认的操作
     */
    CONFIRM
}
