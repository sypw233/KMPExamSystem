package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.utils.DialogManager

/**
 * 应用主模块
 * 聚合所有的依赖注入模块
 */
val appModule = module {
    includes(viewModelModule)
    includes(storageModule)
    includes(apiTestModule)
    includes(authModule)
    includes(courseModule)
    includes(examModule)
    includes(submissionModule)
    includes(notificationModule)
    includes(statisticsModule)
    includes(questionModule)
    includes(questionBankModule)
    includes(fileModule)
    includes(aiGradingModule)
    
    // 全局弹窗管理器
    single { DialogManager() }
}
