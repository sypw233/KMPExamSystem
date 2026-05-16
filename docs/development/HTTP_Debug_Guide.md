# HTTP 请求 Debug 配置说明

## 配置项

在 `HttpClientConfig.kt` 中添加了 `DEBUG` 常量来控制日志输出：

```kotlin
/**
 * Debug 模式开关
 * true: 打印详细的请求和响应信息
 * false: 只打印基本信息
 */
const val DEBUG = true
```

## Debug 模式功能

### 开启 Debug 模式 (`DEBUG = true`)

当 `DEBUG = true` 时，会打印以下详细信息：

**✅ 请求信息：**
- 请求方法（GET, POST, PUT, DELETE 等）
- 完整请求 URL
- 请求头（Headers）
- 请求体（Body）内容（JSON 格式化）

**✅ 响应信息：**
- 响应状态码
- 响应头（Headers）
- 响应体（Body）内容（JSON 格式化）

**✅ 日志格式：**
- 使用 `🌐` emoji 前缀，易于识别
- JSON 内容格式化（prettyPrint）
- 不过滤任何请求

示例输出：
```
🌐 HTTP: REQUEST: http://localhost:8080/api/auth/login
🌐 HTTP: METHOD: HttpMethod(value=POST)
🌐 HTTP: COMMON HEADERS
🌐 HTTP: -> Content-Type: application/json
🌐 HTTP: -> Accept: */*
🌐 HTTP: CONTENT HEADERS
🌐 HTTP: -> Content-Type: application/json; charset=UTF-8
🌐 HTTP: BODY Content-Type: application/json; charset=UTF-8
🌐 HTTP: BODY START
🌐 HTTP: {
🌐 HTTP:   "username": "testuser",
🌐 HTTP:   "password": "password123"
🌐 HTTP: }
🌐 HTTP: BODY END
🌐 HTTP: RESPONSE: 200 OK
🌐 HTTP: METHOD: HttpMethod(value=POST)
🌐 HTTP: FROM: http://localhost:8080/api/auth/login
🌐 HTTP: COMMON HEADERS
🌐 HTTP: -> Content-Type: application/json
🌐 HTTP: BODY Content-Type: application/json; charset=UTF-8
🌐 HTTP: BODY START
🌐 HTTP: {
🌐 HTTP:   "code": 200,
🌐 HTTP:   "message": "登录成功",
🌐 HTTP:   "data": { ... }
🌐 HTTP: }
🌐 HTTP: BODY END
```

---

### 关闭 Debug 模式 (`DEBUG = false`)

当 `DEBUG = false` 时：
- LogLevel 设置为 `INFO`（只记录基本信息）
- 只记录包含 "api" 的请求
- JSON 不格式化（减少性能开销）
- 日志输出精简

---

## 使用场景

### 开发阶段
```kotlin
const val DEBUG = true  // 开启详细日志
```
**用途：**
- 调试 API 请求问题
- 查看完整的请求/响应数据
- 排查网络错误

### 生产环境
```kotlin
const val DEBUG = false  // 关闭详细日志
```
**用途：**
- 减少日志输出
- 提升性能
- 保护敏感数据不被打印

---

## 快速切换

只需修改 `HttpClientConfig.kt` 中的 `DEBUG` 常量：

```kotlin
// 开启 Debug
const val DEBUG = true

// 关闭 Debug
const val DEBUG = false
```

无需修改其他代码，重新运行应用即可生效。

---

## 注意事项

1. **性能影响**：Debug 模式会打印大量日志，可能影响性能
2. **敏感信息**：Debug 模式会打印完整请求内容（包括密码等），注意不要在生产环境开启
3. **日志大小**：大量请求会产生大量日志，影响日志查看

---

## 建议配置

| 环境 | DEBUG 设置 | 说明 |
|-----|-----------|------|
| 本地开发 | `true` | 方便调试 API |
| 测试环境 | `true` | 便于发现问题 |
| 生产环境 | `false` | 保护敏感信息，提升性能 |

---

## 高级配置

如果需要更细粒度的控制，可以扩展配置：

```kotlin
object HttpClientConfig {
    const val DEBUG = true
    const val LOG_HEADERS = true    // 是否打印请求头
    const val LOG_BODY = true       // 是否打印请求体
    const val LOG_RESPONSE = true   // 是否打印响应
}
```

这样可以根据需要灵活控制日志输出的内容。
