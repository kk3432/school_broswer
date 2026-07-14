package com.school.browser.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.school.browser.util.Constants
import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * 管理密码的哈希存储与验证。
 *
 * 使用 BCrypt 进行密码哈希，配置 cost = 12。
 * 密码哈希存储在 EncryptedSharedPreferences 中（双重保护）。
 */
object PasswordManager {

    private const val BCRYPT_COST = 12

    /**
     * 获取 EncryptedSharedPreferences 实例。
     */
    private fun getPrefs(context: Context): android.content.SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            Constants.PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * 检查是否已完成首次设置（已存储密码哈希）。
     */
    fun isSetupCompleted(context: Context? = null): Boolean {
        // 使用 App 的静态 context
        val ctx = context ?: com.school.browser.App.instance
        val prefs = getPrefs(ctx)
        return prefs.getString(Constants.KEY_PASSWORD_HASH, null) != null
    }

    /**
     * 计算密码的 BCrypt 哈希值。
     *
     * @param password 明文密码
     * @return BCrypt 哈希字符串
     */
    fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
    }

    /**
     * 验证输入的密码是否正确。
     *
     * @param context 应用上下文
     * @param input 用户输入的明文密码
     * @return true 表示密码正确
     */
    fun verify(context: Context, input: String): Boolean {
        val prefs = getPrefs(context)
        val storedHash = prefs.getString(Constants.KEY_PASSWORD_HASH, null)
            ?: return false
        val result = BCrypt.verifyer().verify(input.toCharArray(), storedHash)
        return result.verified
    }

    /**
     * 保存密码哈希。
     *
     * @param context 应用上下文
     * @param passwordHash BCrypt 哈希字符串
     */
    fun savePasswordHash(context: Context, passwordHash: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString(Constants.KEY_PASSWORD_HASH, passwordHash).apply()
    }

    /**
     * 修改密码（需要验证旧密码）。
     *
     * @param context 应用上下文
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return true 表示修改成功
     */
    fun changePassword(context: Context, oldPassword: String, newPassword: String): Boolean {
        if (!verify(context, oldPassword)) return false
        val newHash = hash(newPassword)
        savePasswordHash(context, newHash)
        return true
    }

    /**
     * 检查是否处于锁定状态（错误次数过多）。
     *
     * @param context 应用上下文
     * @return true 表示已被锁定
     */
    fun isLocked(context: Context): Boolean {
        val prefs = getPrefs(context)
        val lockTime = prefs.getLong(Constants.KEY_PASSWORD_LOCK_TIME, 0L)
        if (lockTime == 0L) return false
        return System.currentTimeMillis() < lockTime
    }

    /**
     * 锁定密码输入（记录锁定时间）。
     */
    fun lock(context: Context) {
        val prefs = getPrefs(context)
        val lockUntil = System.currentTimeMillis() + Constants.PASSWORD_LOCK_MINUTES * 60 * 1000
        prefs.edit().putLong(Constants.KEY_PASSWORD_LOCK_TIME, lockUntil).apply()
    }

    /**
     * 获取剩余锁定时间（毫秒）。0 表示未锁定。
     */
    fun getRemainingLockTime(context: Context): Long {
        val prefs = getPrefs(context)
        val lockTime = prefs.getLong(Constants.KEY_PASSWORD_LOCK_TIME, 0L)
        val remaining = lockTime - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0L
    }
}