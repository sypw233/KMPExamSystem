package ovo.sypw.kmp.examsystem.data.storage

/**
 * 跨平台本地存储expect声明
 * 各平台需要提供具体的实现
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class LocalStorage {

    /**
     * 保存字符串数据
     * @param key 键
     * @param value 值
     */
    suspend fun saveString(key: String, value: String)

    /**
     * 获取字符串数据
     * @param key 键
     * @return 值，如果不存在则返回null
     */
    suspend fun getString(key: String): String?

    /**
     * 保存布尔值数据
     * @param key 键
     * @param value 值
     */
    suspend fun saveBoolean(key: String, value: Boolean)

    /**
     * 获取布尔值数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    /**
     * 保存整数数据
     * @param key 键
     * @param value 值
     */
    suspend fun saveInt(key: String, value: Int)

    /**
     * 获取整数数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    suspend fun getInt(key: String, defaultValue: Int = 0): Int

    /**
     * 保存长整数数据
     * @param key 键
     * @param value 值
     */
    suspend fun saveLong(key: String, value: Long)

    /**
     * 获取长整数数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long

    /**
     * 删除指定键的数据
     * @param key 键
     */
    suspend fun remove(key: String)

    /**
     * 清除所有数据
     */
    suspend fun clear()

    /**
     * 检查是否包含指定键
     * @param key 键
     * @return 如果包含返回true，否则返回false
     */
    suspend fun contains(key: String): Boolean
}