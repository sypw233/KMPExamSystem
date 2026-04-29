package ovo.sypw.kmp.examsystem.di

import org.koin.dsl.module
import ovo.sypw.kmp.examsystem.data.api.CourseApi
import ovo.sypw.kmp.examsystem.data.repository.CourseRepository
import ovo.sypw.kmp.examsystem.presentation.viewmodel.CourseViewModel

/**
 * 课程模块 Koin 配置
 */
val courseModule = module {
    single { CourseApi(get()) }
    single { CourseRepository(get(), get()) }
    factory { CourseViewModel(get()) }
}
