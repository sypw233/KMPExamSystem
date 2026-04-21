package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.api.StatisticsApi
import ovo.sypw.kmp.examsystem.data.repository.StatisticsRepository
import ovo.sypw.kmp.examsystem.presentation.viewmodel.StatisticsViewModel

/**
 * 统计分析模块 Koin 配置
 */
val statisticsModule = module {
    single { StatisticsApi() }
    single { StatisticsRepository(get(), get()) }
    factory { StatisticsViewModel(get()) }
}
