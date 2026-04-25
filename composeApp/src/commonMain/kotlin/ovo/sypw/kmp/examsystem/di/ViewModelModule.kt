package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.presentation.viewmodel.LoginViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.RegisterViewModel

/**
 * ViewModel模块依赖注入配置
 * 管理所有ViewModel的创建和依赖
 */
val viewModelModule = module {

    factory {
        LoginViewModel(
            authRepository = get()
        )
    }

    factory {
        RegisterViewModel(
            authRepository = get()
        )

    }
}