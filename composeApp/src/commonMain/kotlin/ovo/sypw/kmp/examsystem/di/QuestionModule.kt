package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.api.QuestionApi
import ovo.sypw.kmp.examsystem.data.repository.QuestionRepository
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionViewModel

/**
 * 题目管理模块 Koin 配置（教师/管理员）
 */
val questionModule = module {
    single { QuestionApi() }
    single { QuestionRepository(get(), get()) }
    factory { QuestionViewModel(get()) }
}
