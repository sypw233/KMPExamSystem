package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.api.ApiTestService
import ovo.sypw.kmp.examsystem.domain.repository.ApiTestRepository
import ovo.sypw.kmp.examsystem.domain.repository.impl.ApiTestRepositoryImpl
import ovo.sypw.kmp.examsystem.presentation.viewmodel.ApiTestViewModel

/**
 * API测试模块依赖注入配置
 * 管理API测试相关组件的创建和依赖
 */
val apiTestModule = module {

    /**
     * API测试服务
     * 单例模式，负责执行HTTP请求
     */
    single<ApiTestService> {
        ApiTestService()
    }

    /**
     * API测试Repository
     * 单例模式，负责数据层业务逻辑
     */
    single<ApiTestRepository> {
        ApiTestRepositoryImpl(
            apiTestService = get(),
        )
    }

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