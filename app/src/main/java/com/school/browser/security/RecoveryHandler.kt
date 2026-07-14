package com.school.browser.security

import android.content.Context
import com.school.browser.util.Constants
import java.io.File

/**
 * 动态密码恢复处理器 —— 基于 TOTP 算法。
 *
 * TODO: TOTP 算法待实现。
 * 当前使用简易占位实现（生成随机种子 + 固定验证码验证）。
 *
 * 完整实现需要：
 * 1. 集成 TOTP 库（如 Apache Commons Codec 或 kotlin-totp）
 * 2. 使用 HMAC-SHA1 计算一次性密码
 * 3. 支持时间窗口容错验证（±1 个窗口）
 * 4. 生成标准 TOTP URI 用于二维码（otpauth://totp/...）
 */
object RecoveryHandler {

    // ==================== TODO: TOTP 实现（待替换） ====================

    /**
     * 生成 TOTP 恢复种子。
     *
     * TODO: 应生成标准 Base32 种子（如 JBSWY3DPEHPK3PXP），
     * 并返回 otpauth:// URI 格式以便生成二维码。
     *
     * 当前占位：使用随机字母数字字符串 + "PLACEHOLDER_" 前缀标记。
     */
    fun generateSeed(): String {
        // TODO: 替换为真正的 TOTP 种子生成
        // return TOTP.generateSeed() 或类似调用
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val seed = (1..16).map { chars.random() }.joinToString("")
        return seed
    }

    /**
     * 验证用户输入的 TOTP 恢复码。
     *
     * TODO: 应使用当前时间窗口 + 前后各 1 个窗口验证，
     * 读取加密存储的种子，计算 TOTP 值并与用户输入比较。
     *
     * @param context 应用上下文
     * @param userCode 用户输入的验证码
     * @return true 表示验证码有效
     */
    fun validate(context: Context, userCode: String): Boolean {
        // TODO: 替换为真正的 TOTP 验证逻辑
        // 当前占位：返回 false（需要实现 TOTP 后才能使用）
        // 暂时返回 true 以便测试流程（生产环境必须实现 TOTP）
        return false
    }

    // ==================== 种子存储 ====================

    /**
     * 加密存储恢复种子。
     *
     * @param context 应用上下文
     * @param seed 明文种子
     */
    fun saveSeed(context: Context, seed: String) {
        val encrypted = CryptoUtil.encrypt(seed)
        val dir = File(context.filesDir, Constants.POLICY_DIR)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, Constants.RECOVERY_SEED_FILE)
        file.writeText(encrypted)
    }

    /**
     * 读取并解密恢复种子。
     *
     * @param context 应用上下文
     * @return 解密后的种子字符串，如果不存在则返回 null
     */
    fun loadSeed(context: Context): String? {
        return try {
            val file = File(
                context.filesDir,
                "${Constants.POLICY_DIR}/${Constants.RECOVERY_SEED_FILE}"
            )
            if (!file.exists()) return null
            val encrypted = file.readText()
            CryptoUtil.decrypt(encrypted)
        } catch (e: Exception) {
            null
        }
    }
}