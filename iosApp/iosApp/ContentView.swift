import UIKit
import SwiftUI
import ComposeApp

/**
 * ComposeView: 包装Compose Multiplatform视图控制器的SwiftUI视图
 * 提供与原生iOS应用的桥接功能，支持黑色状态栏图标
 */
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let controller = MainViewControllerKt.MainViewController()
        
        // 配置状态栏样式为黑色图标
        controller.modalPresentationCapturesStatusBarAppearance = true
        
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // 确保状态栏样式更新为黑色图标
        uiViewController.setNeedsStatusBarAppearanceUpdate()
    }
}

/**
 * ContentView: 主内容视图，实现沉浸式导航条和状态栏效果
 * 支持内容延伸到安全区域，同时避免被导航栏覆盖
 */
struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.container, edges: .bottom) // 只忽略底部安全区域
            .statusBarHidden(false) // 保持状态栏可见
            .navigationBarHidden(true) // 隐藏默认导航栏
    }
}



