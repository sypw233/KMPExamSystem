package ovo.sypw.kmp.examsystem.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * HTTP客户端配置类
 * 提供统一的Ktor客户端配置和网络配置常量
 */
object HttpClientConfig {

    /**
     * API基础URL
     */
    const val BASE_URL = ""

    /**
     * 连接超时时间（毫秒）
     */
    const val CONNECT_TIMEOUT = 20_000L

    /**
     * 请求超时时间（毫秒）
     */
    const val REQUEST_TIMEOUT = 20_000L

    /**
     * Socket超时时间（毫秒）
     */
    const val SOCKET_TIMEOUT = 20_000L

    /**
     * 内容类型
     */
    const val CONTENT_TYPE = "*/*"

    /**
     * 用户代理
     */
    const val USER_AGENT = "KMP-App/1.0"

    /**
     * 获取完整的API URL
     * @param endpoint 端点路径
     * @return 完整的API URL
     */
    fun getApiUrl(endpoint: String): String {
        return "$BASE_URL$endpoint"
    }

    /**
     * 创建配置好的HTTP客户端
     * @return 配置完成的HttpClient实例
     */
    fun createHttpClient(): HttpClient {
        return HttpClient {
            // 安装内容协商插件，用于JSON序列化
            install(ContentNegotiation) {
                json(Json {
                    // 忽略未知字段
                    ignoreUnknownKeys = true
                    // 允许结构化映射键
                    allowStructuredMapKeys = true
                    // 美化输出（仅调试时使用）
                    prettyPrint = true
                    // 使用默认值
                    useAlternativeNames = false
                })
            }

            // 安装日志插件
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
                filter { request ->
                    request.url.host.contains("api")
                }
            }

            // 安装超时插件
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT
                connectTimeoutMillis = CONNECT_TIMEOUT
                socketTimeoutMillis = SOCKET_TIMEOUT
            }

            // 安装默认请求插件
            install(DefaultRequest) {
                // 设置默认请求头
                header("Content-Type", CONTENT_TYPE)
                header("Accept", CONTENT_TYPE)
                header("User-Agent", USER_AGENT)

                // 设置基础URL
                url(BASE_URL)
            }

            // 安装HTTP重定向插件
            install(HttpRedirect) {
                checkHttpMethod = false
                allowHttpsDowngrade = false
            }
        }
    }


}