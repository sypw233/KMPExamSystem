package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.api.NotificationApi
import ovo.sypw.kmp.examsystem.data.repository.NotificationRepository
import ovo.sypw.kmp.examsystem.presentation.viewmodel.NotificationViewModel

/**
 * 通知模块 Koin 配置
 */
val notificationModule = module {
    single { NotificationApi(get()) }
    single { NotificationRepository(get(), get()) }
    factory { NotificationViewModel(get()) }
}
