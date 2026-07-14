package com.school.browser.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.school.browser.policy.model.Policy

/**
 * 受控 WebView —— 基于系统 WebView，配置安全项并暴露进度/URL 变化回调。
 *
 * 防绕过措施：
 * - 禁止文件访问
 * - 禁止多窗口
 * - 禁止下载和文件选择器
 * - 禁止长按
 * - 通过 InterceptingClient 拦截所有请求
 */
class ControlledWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    /** 进度变化回调（0-100） */
    var onProgressChanged: ((Int) -> Unit)? = null

    /** URL 变化回调（页面开始加载时） */
    var onUrlChanged: ((String) -> Unit)? = null

    /** 页面加载完成回调 */
    var onPageFinished: ((String) -> Unit)? = null

    /** 拦截到被阻止的 URL 时回调 */
    var onUrlBlocked: ((String) -> Unit)? = null

    /** 当前拦截器引用 */
    private lateinit var interceptingClient: InterceptingClient

    private val blockedPageHtml: String
        get() = BlockedPageHelper.generateBlockedPageHtml(
            mode = interceptingClient.getPolicy().mode
        )

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    fun initSettings(policy: Policy) {
        interceptingClient = InterceptingClient(policy)

        // ========== WebSettings 安全配置 ==========
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = false

            // 禁止文件访问
            allowFileAccess = false
            allowContentAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false

            // 禁止多窗口
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false

            // 缓存策略
            cacheMode = WebSettings.LOAD_DEFAULT
            setAppCacheEnabled(false)

            // 安全增强
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            savePassword = false
            saveFormData = false

            // 禁止缩放控件（防止通过缩放访问隐藏功能）
            builtInZoomControls = false
            displayZoomControls = false
        }

        // ========== WebViewClient（请求拦截） ==========
        webViewClient = interceptingClient

        // ========== WebChromeClient（进度回调） ==========
        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onProgressChanged?.invoke(newProgress)
            }
        }

        // ========== 内部 WebViewClient 覆盖（用于加载完成/拦截显示） ==========
        val originalClient = interceptingClient
        webViewClient = object : android.webkit.WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: android.webkit.WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return true
                return if (originalClient.isAllowed(url)) {
                    // 允许加载，不拦截
                    false
                } else {
                    // URL 被拦截，显示拦截页面
                    onUrlBlocked?.invoke(url)
                    view?.loadDataWithBaseURL(null, generateBlockedPageHtml(url), "text/html", "UTF-8", null)
                    true
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: android.webkit.WebResourceRequest?
            ): android.webkit.WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                if (!originalClient.isAllowed(url)) {
                    return android.webkit.WebResourceResponse("text/plain", "UTF-8", null)
                }
                return null
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    if (originalClient.isAllowed(it)) {
                        onUrlChanged?.invoke(it)
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let {
                    if (originalClient.isAllowed(it)) {
                        onPageFinished?.invoke(it)
                    }
                }
            }
        }

        // ========== 禁用长按（防止文本选择菜单） ==========
        isLongClickable = false
        setOnLongClickListener { true }
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // 长按检测交由 MainActivity 处理（管理入口）
            }
            false
        }

        // ========== 移除滚动条边缘效果 ==========
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER

        // ========== 布局参数 ==========
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    /**
     * 更新策略（管理员修改策略后调用）。
     */
    fun updatePolicy(newPolicy: Policy) {
        interceptingClient.updatePolicy(newPolicy)
    }

    /**
     * 获取当前拦截器（用于外部检查 URL 是否允许）。
     */
    fun getInterceptingClient(): InterceptingClient = interceptingClient

    /**
     * 加载首页（从策略中读取 homepage）。
     */
    fun loadHomepage() {
        val homepage = interceptingClient.getPolicy().homepage
        loadUrl(homepage)
    }

    /**
     * 安全地加载 URL（先检查是否被允许）。
     *
     * @param url 目标 URL
     * @return true 表示已开始加载，false 表示 URL 被拦截
     */
    fun loadUrlSafe(url: String): Boolean {
        return if (interceptingClient.isAllowed(url)) {
            loadUrl(url)
            true
        } else {
            onUrlBlocked?.invoke(url)
            loadDataWithBaseURL(null, generateBlockedPageHtml(url), "text/html", "UTF-8", null)
            false
        }
    }

    /**
     * 生成被拦截页面的 HTML。
     */
    private fun generateBlockedPageHtml(blockedUrl: String): String {
        val policy = interceptingClient.getPolicy()
        return BlockedPageHelper.generateBlockedPageHtml(blockedUrl, policy.mode)
    }
}