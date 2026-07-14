package com.school.browser.util

import android.net.Uri

/**
 * 域名通配符匹配工具。
 * 支持两种匹配模式：
 * 1. 精确匹配：host == pattern
 * 2. 通配符匹配：*.example.com 匹配 example.com 及其所有子域名
 */
object DomainMatcher {

    /**
     * 检查给定主机名是否匹配模式列表中的任意一项。
     *
     * @param host 待检查的主机名（如 "www.example.com"）
     * @param patterns 通配符模式列表
     * @return 是否匹配任意一项
     */
    fun matchesAny(host: String?, patterns: List<String>): Boolean {
        if (host.isNullOrBlank() || patterns.isEmpty()) return false
        val normalizedHost = host.lowercase().trim()
        return patterns.any { pattern -> matchesSingle(normalizedHost, pattern.lowercase().trim()) }
    }

    /**
     * 检查给定 URL 是否匹配模式列表中的任意一项。
     *
     * @param url 完整的 URL 字符串
     * @param patterns 通配符模式列表
     * @return 是否匹配任意一项
     */
    fun matchesAnyUrl(url: String?, patterns: List<String>): Boolean {
        if (url.isNullOrBlank()) return false
        val host = try {
            Uri.parse(url).host
        } catch (e: Exception) {
            null
        }
        return matchesAny(host, patterns)
    }

    /**
     * 单个主机名与单个模式匹配。
     *
     * @param host 标准化后的主机名（小写、无前后空格）
     * @param pattern 标准化后的模式（小写、无前后空格）
     * @return 是否匹配
     */
    private fun matchesSingle(host: String, pattern: String): Boolean {
        return when {
            // 精确匹配
            host == pattern -> true

            // 通配符匹配：*.example.com
            pattern.startsWith("*.") -> {
                val domainPart = pattern.removePrefix("*.")
                // 匹配子域名：sub.example.com
                host.endsWith(".$domainPart") ||
                // 匹配根域名：example.com
                host == domainPart
            }

            // 不匹配
            else -> false
        }
    }
}