package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.api.AuthApi
import ovo.sypw.kmp.examsystem.data.repository.AuthRepository
import ovo.sypw.kmp.examsystem.presentation.viewmodel.LoginViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.RegisterViewModel

/**
 * 认证模块 Koin 配置
 */
val authModule = module {
    // API
    single { AuthApi() }
    
    // Repository
    single { AuthRepository(get(), get()) }
    

}
