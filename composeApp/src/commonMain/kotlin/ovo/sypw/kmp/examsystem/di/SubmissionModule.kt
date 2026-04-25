package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.api.SubmissionApi
import ovo.sypw.kmp.examsystem.data.repository.SubmissionRepository
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ExamTakingViewModel

/**
 * 答题提交模块 Koin 配置
 */
val submissionModule = module {
    single { SubmissionApi() }
    single { SubmissionRepository(get(), get()) }
    factory { ExamTakingViewModel(get(), get()) }
    factory { ovo.sypw.kmp.examsystem.presentation.viewmodel.GradeSubmissionViewModel(get(), get(), get()) }
}
