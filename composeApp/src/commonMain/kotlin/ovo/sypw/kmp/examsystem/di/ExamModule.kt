package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.api.ExamApi
import ovo.sypw.kmp.examsystem.data.repository.ExamRepository
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamViewModel

/**
 * 考试模块 Koin 配置
 */
val examModule = module {
    single { ExamApi() }
    single { ExamRepository(get(), get()) }
    factory { ExamViewModel(get()) }
}
