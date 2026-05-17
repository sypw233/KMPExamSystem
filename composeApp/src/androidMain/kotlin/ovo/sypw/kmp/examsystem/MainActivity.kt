package ovo.sypw.kmp.examsystem

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.ComposeView
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.CSJAdError
import com.bytedance.sdk.openadsdk.CSJSplashAd
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.mediation.ad.MediationAdSlot
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

class MainActivity : ComponentActivity() {
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FileKit.init(this)
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        val composeView = ComposeView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setContent {
                App()
            }
        }
        val splashContainer = FrameLayout(this).apply {
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(android.graphics.Color.WHITE)
        }
        root.addView(composeView)
        root.addView(splashContainer)
        setContentView(root)

        showColdStartSplashAd(splashContainer)
    }

    private fun showColdStartSplashAd(container: FrameLayout) {
        val appId = BuildConfig.PANGLE_APP_ID
        val splashAdId = BuildConfig.PANGLE_SPLASH_AD_ID
        if (appId.isBlank() || splashAdId.isBlank()) return
        if (!isPangleNativeAbiSupported()) {
            Log.w(TAG, "Skip Pangle splash: unsupported ABI ${Build.SUPPORTED_ABIS.joinToString()}")
            return
        }

        val dismissRunnable = Runnable { dismissSplash(container) }
        mainHandler.postDelayed(dismissRunnable, SPLASH_TIMEOUT_MS + 800L)

        val config = TTAdConfig.Builder()
            .appId(appId)
            .appName(getString(applicationInfo.labelRes))
            .useMediation(true)
            .supportMultiProcess(false)
            .debug(BuildConfig.DEBUG)
            .build()

        runCatching {
            TTAdSdk.init(applicationContext, config)
            TTAdSdk.start(object : TTAdSdk.Callback {
                override fun success() {
                    Log.i(TAG, "Pangle init success")
                    loadSplashAd(container, splashAdId, dismissRunnable)
                }

                override fun fail(code: Int, msg: String?) {
                    Log.w(TAG, "Pangle init failed: $code $msg")
                    dismissSplash(container)
                }
            })
        }.onFailure {
            Log.e(TAG, "Pangle init error", it)
            dismissSplash(container)
        }
    }

    private fun loadSplashAd(container: FrameLayout, splashAdId: String, dismissRunnable: Runnable) {
        val metrics = resources.displayMetrics
        val adSlot = AdSlot.Builder()
            .setCodeId(splashAdId)
            .setImageAcceptedSize(metrics.widthPixels, metrics.heightPixels)
            .setExpressViewAcceptedSize(metrics.widthPixels / metrics.density, metrics.heightPixels / metrics.density)
            .setMediationAdSlot(
                MediationAdSlot.Builder()
                    .setSplashShakeButton(false)
                    .build()
            )
            .build()

        TTAdSdk.getAdManager()
            .createAdNative(this)
            .loadSplashAd(adSlot, object : TTAdNative.CSJSplashAdListener {
                override fun onSplashLoadSuccess(ad: CSJSplashAd?) {
                    Log.i(TAG, "Pangle splash load success")
                }

                override fun onSplashLoadFail(error: CSJAdError?) {
                    Log.w(TAG, "Pangle splash load failed: ${error?.code} ${error?.msg}")
                    dismissSplash(container)
                }

                override fun onSplashRenderSuccess(ad: CSJSplashAd?) {
                    if (ad == null) {
                        dismissSplash(container)
                        return
                    }
                    Log.i(TAG, "Pangle splash render success")
                    mainHandler.removeCallbacks(dismissRunnable)
                    ad.setSplashAdListener(object : CSJSplashAd.SplashAdListener {
                        override fun onSplashAdShow(ad: CSJSplashAd?) {
                            Log.i(TAG, "Pangle splash shown")
                        }

                        override fun onSplashAdClick(ad: CSJSplashAd?) {
                            Log.i(TAG, "Pangle splash clicked")
                        }

                        override fun onSplashAdClose(ad: CSJSplashAd?, closeType: Int) {
                            Log.i(TAG, "Pangle splash closed: $closeType")
                            dismissSplash(container)
                        }
                    })
                    container.removeAllViews()
                    container.visibility = View.VISIBLE
                    ad.showSplashView(container)
                }

                override fun onSplashRenderFail(ad: CSJSplashAd?, error: CSJAdError?) {
                    Log.w(TAG, "Pangle splash render failed: ${error?.code} ${error?.msg}")
                    dismissSplash(container)
                }
            }, SPLASH_TIMEOUT_MS)
    }

    private fun isPangleNativeAbiSupported(): Boolean {
        val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull().orEmpty()
        return primaryAbi == "arm64-v8a" || primaryAbi == "armeabi-v7a"
    }

    private fun dismissSplash(container: FrameLayout) {
        mainHandler.removeCallbacksAndMessages(null)
        container.removeAllViews()
        container.visibility = View.GONE
    }

    companion object {
        private const val TAG = "PangleSplash"
        private const val SPLASH_TIMEOUT_MS = 3500
    }
}
