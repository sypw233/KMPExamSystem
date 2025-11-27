package ovo.sypw.kmp.examsystem.data.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/**
 * Desktop平台的LocalStorage实现
 * 使用Properties文件进行数据存储
 */
actual class LocalStorage {

    companion object {
        private const val STORAGE_FILE_NAME = "bsp_local_storage.properties"
    }

    private val storageFile: File by lazy {
        val userHome = System.getProperty("user.home")
        val appDir = File(userHome, ".bsp")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        File(appDir, STORAGE_FILE_NAME)
    }

    private val properties = Properties()

    init {
        loadProperties()
    }

    /**
     * 加载Properties文件
     */
    private fun loadProperties() {
        try {
            if (storageFile.exists()) {
                FileInputStream(storageFile).use { input ->
                    properties.load(input)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 保存Properties到文件
     */
    private suspend fun saveProperties() {
        withContext(Dispatchers.IO) {
            try {
                FileOutputStream(storageFile).use { output ->
                    properties.store(output, "BSP Local Storage")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 保存字符串数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveString(key: String, value: String) {
        properties.setProperty(key, value)
        saveProperties()
    }

    /**
     * 获取字符串数据
     * @param key 键
     * @return 值，如果不存在则返回null
     */
    actual suspend fun getString(key: String): String? {
        return properties.getProperty(key)
    }

    /**
     * 保存布尔值数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveBoolean(key: String, value: Boolean) {
        properties.setProperty(key, value.toString())
        saveProperties()
    }

    /**
     * 获取布尔值数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    actual suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val value = properties.getProperty(key)
        return value?.toBooleanStrictOrNull() ?: defaultValue
    }

    /**
     * 保存整数数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveInt(key: String, value: Int) {
        properties.setProperty(key, value.toString())
        saveProperties()
    }

    /**
     * 获取整数数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    actual suspend fun getInt(key: String, defaultValue: Int): Int {
        val value = properties.getProperty(key)
        return value?.toIntOrNull() ?: defaultValue
    }

    /**
     * 保存长整数数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveLong(key: String, value: Long) {
        properties.setProperty(key, value.toString())
        saveProperties()
    }

    /**
     * 获取长整数数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    actual suspend fun getLong(key: String, defaultValue: Long): Long {
        val value = properties.getProperty(key)
        return value?.toLongOrNull() ?: defaultValue
    }

    /**
     * 删除指定键的数据
     * @param key 键
     */
    actual suspend fun remove(key: String) {
        properties.remove(key)
        saveProperties()
    }

    /**
     * 清除所有数据
     */
    actual suspend fun clear() {
        properties.clear()
        saveProperties()
    }

    /**
     * 检查是否包含指定键
     * @param key 键
     * @return 如果包含返回true，否则返回false
     */
    actual suspend fun contains(key: String): Boolean {
        return properties.containsKey(key)
    }
}