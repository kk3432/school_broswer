package com.school.browser.policy.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 策略数据模型 - 统一定义浏览器的网址拦截规则。
 *
 * @property version 策略版本号，用于判断是否需要更新
 * @property mode 模式："whitelist"（白名单）或 "blacklist"（黑名单）
 * @property whitelist 白名单域名列表，支持通配符（如 *.school.edu.cn）
 * @property blacklist 黑名单域名列表，支持通配符
 * @property homepage 默认首页地址
 * @property shortcutUrls 快捷地址列表（名称 -> URL 映射）
 * @property source 策略来源标识："local"、"remote"、"smb"
 * @property updatedAt 最后更新时间（ISO 8601 格式）
 */
@Serializable
data class Policy(
    val version: Int = 1,
    val mode: String = "whitelist",
    val whitelist: List<String> = emptyList(),
    val blacklist: List<String> = emptyList(),
    val homepage: String = "https://www.school.edu.cn",
    val shortcutUrls: List<ShortcutEntry> = emptyList(),
    val source: String = "local",
    val updatedAt: String = ""
) {

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
        }

        /** 从 JSON 字符串反序列化为 Policy 对象 */
        fun fromJson(jsonString: String): Policy =
            json.decodeFromString<Policy>(jsonString)

        /** 将 Policy 对象序列化为 JSON 字符串 */
        fun toJson(policy: Policy): String =
            json.encodeToString(policy)

        /** 创建默认策略（空策略，强制要求远程配置或本地文件策略） */
        fun defaultPolicy(): Policy = Policy(
            version = 1,
            mode = "whitelist",
            whitelist = emptyList(),
            blacklist = emptyList(),
            homepage = "https://www.school.edu.cn",
            shortcutUrls = emptyList(),
            source = "local",
            updatedAt = ""
        )
    }
}

/**
 * 快捷地址条目
 *
 * @property name 显示名称
 * @property url 完整 URL 地址
 */
@Serializable
data class ShortcutEntry(
    val name: String,
    val url: String
)