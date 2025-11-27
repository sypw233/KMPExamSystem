package ovo.sypw.kmp.examsystem.data.dto.result

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * SaResult响应数据类
 * 匹配后端实际返回的数据格式
 * 标准格式：{ "code": 200, "msg": "success", "data": {...} }
 */
@Serializable
data class SaResult(
    /**
     * 响应状态码
     */
    val code: Int,

    /**
     * 响应消息
     */
    val msg: String,

    /**
     * 响应数据，可以是任意JSON结构
     */
    val data: JsonElement? = null
)

/**
 * 扩展函数：检查SaResult是否成功
 * @return 是否成功（通常code为200表示成功）
 */
fun SaResult.isSuccess(): Boolean {
    return code == 200
}

/**
 * 获取SaResult中的数据
 * @return JsonElement类型的数据
 */
fun SaResult.getData(): JsonElement? {
    return data
}

/**
 * 检查是否为错误响应
 * @return 是否为错误
 */
fun SaResult.isError(): Boolean {
    return code != 200
}

/**
 * 获取错误消息
 * @return 错误消息
 */
fun SaResult.getErrorMessage(): String {
    return if (isError()) msg else ""
}

/**
 * 将SaResult中的data字段反序列化为指定类型
 * @param T 目标类型
 * @return 反序列化后的对象，如果data为null或反序列化失败则返回null
 */
inline fun <reified T> SaResult.parseData(): T? {
    return try {
        if (data == null) {
//            println("[SaResult.parseData] data字段为null")
            return null
        }
//        println("[SaResult.parseData] 原始data: $data")

        // 创建宽松的Json配置，允许非标准JSON格式
        val lenientJson = Json {
            isLenient = true
            ignoreUnknownKeys = true
            allowStructuredMapKeys = true
        }

        val result = lenientJson.decodeFromJsonElement<T>(data)
//        println("[SaResult.parseData] 反序列化成功: $result")
        result
    } catch (e: Exception) {
        println("[SaResult.parseData] 反序列化失败: ${e.message}")
        println("[SaResult.parseData] 异常详情: $e")
        println("[SaResult.parseData] 目标类型: ${T::class.simpleName}")
        null
    }
}

/**
 * 将SaResult中的data字段反序列化为指定类型，并提供默认值
 * @param T 目标类型
 * @param defaultValue 默认值，当data为null或反序列化失败时返回
 * @return 反序列化后的对象或默认值
 */
inline fun <reified T> SaResult.parseDataOrDefault(defaultValue: T): T {
    return parseData<T>() ?: defaultValue
}