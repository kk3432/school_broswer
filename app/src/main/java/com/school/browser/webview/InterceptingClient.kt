package com.school.browser.webview

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.school.browser.policy.model.Policy
import com.school.browser.util.DomainMatcher

/**
 * WebView 请求拦截器。
 * 根据当前策略决定是否允许加载某个 URL。
 * 在白名单模式下仅允许白名单内的域名；在黑名单模式下拦截黑名单内的域名。
 */
class InterceptingClient(private var policy: Policy) : WebViewClient() {

    /**
     * 更新当前策略（当管理员修改策略后调用）。
     */
    fun updatePolicy(newPolicy: Policy) {
        policy = newPolicy
    }

    /**
     * 获取当前策略。
     */
    fun getPolicy(): Policy = policy

    /**
     * 页面导航拦截 —— 决定是否允许加载新的 URL。
     * 返回 true 表示拦截（阻止加载），返回 false 表示允许加载。
     */
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return true
        return !isAllowed(url)
    }

    /**
     * 子资源请求拦截 —— 拦截图片、CSS、JS 等资源请求。
     * 返回 null 表示不拦截，返回空 WebResourceResponse 表示阻止加载。
     */
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null
        if (!isAllowed(url)) {
            // 返回空响应阻止子资源加载
            return WebResourceResponse("text/plain", "UTF-8", null)
        }
        return super.shouldInterceptRequest(view, request)
    }

    /**
     * 检查指定 URL 是否被当前策略允许。
     *
     * @param url 完整的 URL
     * @return true 表示允许访问
     */
    fun isAllowed(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val host = try {
            Uri.parse(url).host
        } catch (e: Exception) {
            null
        } ?: return false

        return if (policy.mode == "whitelist") {
            // 白名单模式：必须在白名单中
            DomainMatcher.matchesAny(host, policy.whitelist)
        } else {
            // 黑名单模式：不能出现在黑名单中
            !DomainMatcher.matchesAny(host, policy.blacklist)
        }
    }
}