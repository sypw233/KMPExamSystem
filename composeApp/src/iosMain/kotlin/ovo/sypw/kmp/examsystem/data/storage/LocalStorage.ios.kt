package ovo.sypw.kmp.examsystem.data.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

/**
 * iOS平台的LocalStorage实现
 * 使用NSUserDefaults进行数据存储
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class LocalStorage {

    private val userDefaults = NSUserDefaults.standardUserDefaults

    /**
     * 保存字符串数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveString(key: String, value: String) {
        withContext(Dispatchers.Main) {
            userDefaults.setObject(value, key)
            userDefaults.synchronize()
        }
    }

    /**
     * 获取字符串数据
     * @param key 键
     * @return 值，如果不存在则返回null
     */
    actual suspend fun getString(key: String): String? {
        return withContext(Dispatchers.Main) {
            userDefaults.stringForKey(key)
        }
    }

    /**
     * 保存布尔值数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveBoolean(key: String, value: Boolean) {
        withContext(Dispatchers.Main) {
            userDefaults.setBool(value, key)
            userDefaults.synchronize()
        }
    }

    /**
     * 获取布尔值数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    actual suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return withContext(Dispatchers.Main) {
            if (userDefaults.objectForKey(key) != null) {
                userDefaults.boolForKey(key)
            } else {
                defaultValue
            }
        }
    }

    /**
     * 保存整数数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveInt(key: String, value: Int) {
        withContext(Dispatchers.Main) {
            userDefaults.setInteger(value.toLong(), key)
            userDefaults.synchronize()
        }
    }

    /**
     * 获取整数数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    actual suspend fun getInt(key: String, defaultValue: Int): Int {
        return withContext(Dispatchers.Main) {
            if (userDefaults.objectForKey(key) != null) {
                userDefaults.integerForKey(key).toInt()
            } else {
                defaultValue
            }
        }
    }

    /**
     * 保存长整数数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveLong(key: String, value: Long) {
        withContext(Dispatchers.Main) {
            userDefaults.setInteger(value, key)
            userDefaults.synchronize()
        }
    }

    /**
     * 获取长整数数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    actual suspend fun getLong(key: String, defaultValue: Long): Long {
        return withContext(Dispatchers.Main) {
            if (userDefaults.objectForKey(key) != null) {
                userDefaults.integerForKey(key)
            } else {
                defaultValue
            }
        }
    }

    /**
     * 删除指定键的数据
     * @param key 键
     */
    actual suspend fun remove(key: String) {
        withContext(Dispatchers.Main) {
            userDefaults.removeObjectForKey(key)
            userDefaults.synchronize()
        }
    }

    /**
     * 清除所有数据
     */
    actual suspend fun clear() {
        withContext(Dispatchers.Main) {

        }
    }

    /**
     * 检查是否包含指定键
     * @param key 键
     * @return 如果包含返回true，否则返回false
     */
    actual suspend fun contains(key: String): Boolean {
        return withContext(Dispatchers.Main) {
            userDefaults.objectForKey(key) != null
        }
    }
}