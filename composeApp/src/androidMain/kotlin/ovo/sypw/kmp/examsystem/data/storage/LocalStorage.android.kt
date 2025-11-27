package ovo.sypw.kmp.examsystem.data.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android平台的LocalStorage实现
 * 使用SharedPreferences进行数据存储
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class LocalStorage(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "bsp_local_storage"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 保存字符串数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveString(key: String, value: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putString(key, value)
                .apply()
        }
    }

    /**
     * 获取字符串数据
     * @param key 键
     * @return 值，如果不存在则返回null
     */
    actual suspend fun getString(key: String): String? {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString(key, null)
        }
    }

    /**
     * 保存布尔值数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveBoolean(key: String, value: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putBoolean(key, value)
                .apply()
        }
    }

    /**
     * 获取布尔值数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    actual suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(key, defaultValue)
        }
    }

    /**
     * 保存整数数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveInt(key: String, value: Int) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putInt(key, value)
                .apply()
        }
    }

    /**
     * 获取整数数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    actual suspend fun getInt(key: String, defaultValue: Int): Int {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getInt(key, defaultValue)
        }
    }

    /**
     * 保存长整数数据
     * @param key 键
     * @param value 值
     */
    actual suspend fun saveLong(key: String, value: Long) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .putLong(key, value)
                .apply()
        }
    }

    /**
     * 获取长整数数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 值，如果不存在则返回默认值
     */
    actual suspend fun getLong(key: String, defaultValue: Long): Long {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getLong(key, defaultValue)
        }
    }

    /**
     * 删除指定键的数据
     * @param key 键
     */
    actual suspend fun remove(key: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .remove(key)
                .apply()
        }
    }

    /**
     * 清除所有数据
     */
    actual suspend fun clear() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit()
                .clear()
                .apply()
        }
    }

    /**
     * 检查是否包含指定键
     * @param key 键
     * @return 如果包含返回true，否则返回false
     */
    actual suspend fun contains(key: String): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.contains(key)
        }
    }
}