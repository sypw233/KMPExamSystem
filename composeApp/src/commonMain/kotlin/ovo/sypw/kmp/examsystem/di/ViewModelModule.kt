package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ApiTestViewModel

/**
 * ViewModel模块依赖注入配置
 * 管理所有ViewModel的创建和依赖
 */
val viewModelModule = module {

    /**
     * API测试ViewModel
     * 工厂模式，每次注入时创建新实例
     */
    factory {
        ApiTestViewModel(
            apiTestRepository = get()
        )
    }
}