package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.api.HttpClientConfig
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
    includes(userManageModule)
    
    // 全局共享的HTTP客户端实例,所有API服务共用,避免资源泄漏
    single { HttpClientConfig.createHttpClient() }
    
    // 全局弹窗管理器
    single { DialogManager() }
}
