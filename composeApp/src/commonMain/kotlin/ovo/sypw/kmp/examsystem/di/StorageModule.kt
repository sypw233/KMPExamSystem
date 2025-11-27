package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.storage.LocalStorage
import ovo.sypw.kmp.examsystem.data.storage.TokenStorage
import ovo.sypw.kmp.examsystem.data.storage.TokenStorageImpl

/**
 * 创建平台特定的LocalStorage实例
 * 各平台需要提供具体实现
 */
expect fun createLocalStorage(): LocalStorage

/**
 * 存储模块的依赖注入配置
 * 提供LocalStorage和TokenStorage的实例
 */
val storageModule = module {

    /**
     * 提供LocalStorage单例实例
     * 各平台会有不同的实现
     */
    single<LocalStorage> {
        // 平台特定的LocalStorage实现将通过expect/actual机制提供
        createLocalStorage()
    }

    /**
     * 提供TokenStorage单例实例
     * 依赖于LocalStorage
     */
    single<TokenStorage> {
        TokenStorageImpl(get())
    }
}