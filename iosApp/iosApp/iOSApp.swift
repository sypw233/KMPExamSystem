import SwiftUI

/**
 * iOSApp: 应用程序入口点
 * 配置全局的沉浸式导航和状态栏设置
 */
@main
struct iOSApp: App {
    
    init() {
        // 配置全局导航栏外观，支持沉浸式效果
        configureNavigationBarAppearance()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .preferredColorScheme(.light) // 可根据需要调整主题
        }
    }
    
    /**
     * 配置导航栏外观，实现沉浸式效果
     * 设置透明背景和自定义样式
     */
    private func configureNavigationBarAppearance() {
        let appearance = UINavigationBarAppearance()
        appearance.configureWithTransparentBackground()
        appearance.backgroundColor = UIColor.clear
        appearance.shadowColor = UIColor.clear
        
        // 应用到所有导航栏状态
        UINavigationBar.appearance().standardAppearance = appearance
        UINavigationBar.appearance().compactAppearance = appearance
        UINavigationBar.appearance().scrollEdgeAppearance = appearance
        
        // 配置状态栏样式
        UINavigationBar.appearance().barStyle = .default
        UINavigationBar.appearance().isTranslucent = true
    }
}