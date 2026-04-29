package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.api.UserManageApi
import ovo.sypw.kmp.examsystem.data.repository.UserManageRepository
import ovo.sypw.kmp.examsystem.presentation.viewmodel.UserManageViewModel

/**
 * 用户管理模块 DI（管理员专用）
 */
val userManageModule = module {
    single { UserManageApi(get()) }
    single { UserManageRepository(get(), get()) }
    factory { UserManageViewModel(get()) }
}
