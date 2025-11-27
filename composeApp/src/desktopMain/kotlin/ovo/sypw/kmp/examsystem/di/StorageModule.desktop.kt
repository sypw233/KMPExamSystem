package ovo.sypw.kmp.examsystem.di

import ovo.sypw.kmp.examsystem.data.storage.LocalStorage

/**
 * Desktop平台的LocalStorage创建函数
 * 直接创建LocalStorage实例，无需额外参数
 */
actual fun createLocalStorage(): LocalStorage {
    return LocalStorage()
}