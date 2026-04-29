package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.api.AiGradingApi
import ovo.sypw.kmp.examsystem.data.api.FileApi
import ovo.sypw.kmp.examsystem.data.api.QuestionBankApi
import ovo.sypw.kmp.examsystem.data.repository.AiGradingRepository
import ovo.sypw.kmp.examsystem.data.repository.FileRepository
import ovo.sypw.kmp.examsystem.data.repository.QuestionBankRepository
import ovo.sypw.kmp.examsystem.presentation.viewmodel.QuestionBankViewModel
import ovo.sypw.kmp.examsystem.presentation.viewmodel.SystemSettingsViewModel

/**
 * 题库管理模块 DI
 */
val questionBankModule = module {
    single { QuestionBankApi(get()) }
    single { QuestionBankRepository(get(), get()) }
    factory { QuestionBankViewModel(get(), get()) }
}

/**
 * 文件管理模块 DI
 */
val fileModule = module {
    single { FileApi(get()) }
    single { FileRepository(get(), get()) }
}

/**
 * AI 辅助判题模块 DI
 */
val aiGradingModule = module {
    single { AiGradingApi(get()) }
    single { AiGradingRepository(get(), get()) }
    factory { SystemSettingsViewModel(get()) }
}
