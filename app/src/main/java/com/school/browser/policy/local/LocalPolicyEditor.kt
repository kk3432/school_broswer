package com.school.browser.policy.local

import android.content.Context
import com.school.browser.policy.model.Policy
import com.school.browser.policy.model.RemoteSourceConfig
import com.school.browser.security.CryptoUtil
import com.school.browser.util.Constants
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 本地策略的加密读写工具。
 *
 * 本地策略以 AES-256-GCM 加密存储在内部存储文件中。
 * 远程源配置存储在 SharedPreferences 中。
 * 同时支持从 .txt 文件读取域名列表作为回退策略。
 */
object LocalPolicyEditor {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    /**
     * 加密并保存本地策略。
     */
    fun save(context: Context, policy: Policy) {
        val policyJson = Policy.toJson(policy)
        val encrypted = CryptoUtil.encrypt(policyJson)

        val dir = File(context.filesDir, Constants.POLICY_DIR)
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, Constants.LOCAL_POLICY_FILE)
        file.writeText(encrypted)
    }

    /**
     * 读取并解密本地策略。
     */
    fun load(context: Context): Policy {
        return try {
            val file = File(context.filesDir, "${Constants.POLICY_DIR}/${Constants.LOCAL_POLICY_FILE}")
            if (!file.exists()) {
                return Policy.defaultPolicy()
            }

            val encrypted = file.readText()
            val policyJson = CryptoUtil.decrypt(encrypted)
            Policy.fromJson(policyJson)
        } catch (e: Exception) {
            Policy.defaultPolicy()
        }
    }

    /**
     * 缓存远程拉取到的策略。
     */
    fun cacheRemotePolicy(context: Context, policy: Policy) {
        val policyJson = Policy.toJson(policy)
        val encrypted = CryptoUtil.encrypt(policyJson)

        val dir = File(context.filesDir, Constants.POLICY_DIR)
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, Constants.REMOTE_POLICY_CACHE_FILE)
        file.writeText(encrypted)
    }

    /**
     * 读取缓存的远程策略。
     */
    fun loadCachedRemotePolicy(context: Context): Policy? {
        return try {
            val file = File(
                context.filesDir,
                "${Constants.POLICY_DIR}/${Constants.REMOTE_POLICY_CACHE_FILE}"
            )
            if (!file.exists()) return null

            val encrypted = file.readText()
            val policyJson = CryptoUtil.decrypt(encrypted)
            Policy.fromJson(policyJson)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 保存远程源配置。
     */
    fun saveRemoteConfig(context: Context, config: RemoteSourceConfig) {
        val configJson = json.encodeToString(RemoteSourceConfig.serializer(), config)
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(Constants.KEY_REMOTE_CONFIG, configJson).apply()
    }

    /**
     * 读取远程源配置。
     */
    fun loadRemoteConfig(context: Context): RemoteSourceConfig {
        return try {
            val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            val configJson = prefs.getString(Constants.KEY_REMOTE_CONFIG, null)
            if (configJson != null) {
                json.decodeFromString(RemoteSourceConfig.serializer(), configJson)
            } else {
                RemoteSourceConfig.defaultConfig()
            }
        } catch (e: Exception) {
            RemoteSourceConfig.defaultConfig()
        }
    }

    /**
     * 从本地 .txt 文件读取域名策略。
     *
     * 文件名规则：
     * - 白名单模式：policy_data/true_website.txt
     * - 黑名单模式：policy_data/notrue_website.txt
     *
     * 文件格式：每行一个域名，支持 # 开头的注释行和空行，支持通配符 *.domain.com
     *
     * @param context 应用上下文
     * @param mode 当前策略模式 ("whitelist" 或 "blacklist")
     * @return 解析出的 Policy，如果文件不存在或为空则返回 null
     */
    fun loadPolicyFromFile(context: Context, mode: String): Policy? {
        val fileName = if (mode == "whitelist") {
            "true_website.txt"
        } else {
            "notrue_website.txt"
        }

        return try {
            val dir = File(context.filesDir, Constants.POLICY_DIR)
            val file = File(dir, fileName)
            if (!file.exists()) return null

            val domains = file.readLines()
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.startsWith("#") }

            if (domains.isEmpty()) return null

            // 从第一个域名推测首页
            val guessedHomepage = if (domains[0].contains(".")) {
                val first = domains[0].removePrefix("*.")
                "https://$first"
            } else {
                "https://www.school.edu.cn"
            }

            Policy(
                version = 0,
                mode = mode,
                whitelist = if (mode == "whitelist") domains else emptyList(),
                blacklist = if (mode == "blacklist") domains else emptyList(),
                homepage = guessedHomepage,
                shortcutUrls = emptyList(),
                source = "local_file",
                updatedAt = ""
            )
        } catch (e: Exception) {
            null
        }
    }
}