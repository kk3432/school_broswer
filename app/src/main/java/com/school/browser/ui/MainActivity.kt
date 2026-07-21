package com.school.browser.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.school.browser.R
import com.school.browser.policy.manager.PolicyManager
import com.school.browser.policy.model.Policy
import com.school.browser.policy.model.ShortcutEntry
import com.school.browser.security.PasswordManager
import com.school.browser.ui.admin.LocalManagementActivity
import com.school.browser.ui.dialog.PasswordDialog
import com.school.browser.util.Constants
import com.school.browser.webview.ControlledWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 主浏览器 Activity —— 全屏 WebView + 地址栏 + 进度条 + 快捷地址。
 *
 * 管理入口：长按 WebView 空白处 3 秒弹出密码验证。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: ControlledWebView
    private lateinit var progressBar: ProgressBar
    private lateinit var urlEditText: android.widget.EditText
    private lateinit var shortcutsContainer: LinearLayout
    private lateinit var shortcutsScroll: android.widget.HorizontalScrollView
    private lateinit var lockIcon: ImageView

    /** 标记 URL 是否正在由程序设置（避免循环更新） */
    private var isProgrammaticUrlChange = false

    /** 长按检测变量 */
    private var longPressStartTime = 0L
    private var longPressTriggered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initWebView()
        setupUrlBar()
        setupShortcuts()
        setupLongPressDetection()
        initPolicyAndLoad()
    }

    // ========================= 初始化视图 =========================

    private fun initViews() {
        webView = findViewById(R.id.controlled_webview)
        progressBar = findViewById(R.id.progress_bar)
        urlEditText = findViewById(R.id.et_url)
        shortcutsContainer = findViewById(R.id.shortcuts_container)
        shortcutsScroll = findViewById(R.id.shortcuts_scroll)
        lockIcon = findViewById(R.id.iv_lock_icon)

        val btnRefresh: android.widget.ImageButton = findViewById(R.id.btn_refresh)
        btnRefresh.setOnClickListener { webView.reload() }

        val btnShortcuts: android.widget.ImageButton = findViewById(R.id.btn_shortcuts_dropdown)
        btnShortcuts.setOnClickListener { showShortcutsDropdown() }
    }

    // ========================= WebView 初始化 =========================

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        val initialPolicy = getInitialPolicy()
        webView.initSettings(initialPolicy)

        // 进度条回调
        webView.onProgressChanged = { progress ->
            progressBar.progress = progress
            if (progress < 100) {
                progressBar.visibility = View.VISIBLE
            } else {
                // 延迟隐藏，实现平滑过渡
                progressBar.postDelayed({
                    progressBar.visibility = View.GONE
                    progressBar.progress = 0
                }, Constants.PROGRESS_ANIM_DURATION)
            }
        }

        // URL 变化回调（更新地址栏）
        webView.onUrlChanged = { url ->
            if (!isProgrammaticUrlChange) {
                urlEditText.setText(url)
                urlEditText.setSelection(urlEditText.text.length)
            }
        }

        // 页面加载完成
        webView.onPageFinished = { url ->
            updateLockIcon(url)
        }

        // URL 被拦截回调
        webView.onUrlBlocked = { blockedUrl ->
            Toast.makeText(
                this,
                getString(R.string.page_blocked_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ========================= 地址栏 =========================

    private fun setupUrlBar() {
        urlEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val input = urlEditText.text.toString().trim()
                navigateToUrl(input)
                true
            } else {
                false
            }
        }

        // 点击时全选方便输入
        urlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                urlEditText.selectAll()
            }
        }
    }

    /**
     * 导航到指定 URL（会经过策略检查）。
     */
    private fun navigateToUrl(url: String) {
        if (url.isBlank()) return

        // 补全协议头
        val finalUrl = when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.contains(".") -> "https://$url"
            else -> {
                Toast.makeText(this, "请输入有效的网址", Toast.LENGTH_SHORT).show()
                return
            }
        }

        isProgrammaticUrlChange = true
        val success = webView.loadUrlSafe(finalUrl)
        if (success) {
            urlEditText.setText(finalUrl)
        }
        isProgrammaticUrlChange = false

        // 隐藏键盘
        urlEditText.clearFocus()
    }

    /**
     * 更新地址栏锁图标。
     */
    private fun updateLockIcon(url: String?) {
        if (url != null && url.startsWith("https://")) {
            lockIcon.setImageResource(R.drawable.ic_lock)
        } else {
            // 非 HTTPS 显示警告图标（使用相同图标但不同颜色）
            lockIcon.setImageResource(R.drawable.ic_lock)
            lockIcon.setColorFilter(getColor(R.color.accent_blue))
        }
    }

    // ========================= 快捷地址 =========================

    private fun setupShortcuts() {
        refreshShortcuts()
    }

    /**
     * 刷新快捷地址标签组。
     */
    private fun refreshShortcuts() {
        shortcutsContainer.removeAllViews()

        val policy = PolicyManager.getCurrentPolicy()
        val shortcuts = policy.shortcutUrls

        if (shortcuts.isEmpty()) {
            shortcutsScroll.visibility = View.GONE
            return
        }

        shortcutsScroll.visibility = View.VISIBLE
        val maxDisplay = minOf(shortcuts.size, Constants.MAX_SHORTCUT_DISPLAY)
        for (i in 0 until maxDisplay) {
            val entry = shortcuts[i]
            val chip = createShortcutChip(entry)
            shortcutsContainer.addView(chip)
        }
    }

    /**
     * 创建单个快捷地址标签。
     */
    private fun createShortcutChip(entry: ShortcutEntry): Chip {
        val chip = Chip(this)
        chip.text = entry.name
        chip.isClickable = true
        chip.isCheckable = false
        chip.setChipBackgroundColorResource(R.color.shortcut_chip_background)
        chip.setTextColor(getColor(R.color.shortcut_chip_text))
        chip.textSize = 13f
        chip.setPadding(8, 0, 8, 0)
        chip.chipCornerRadius = 16f
        chip.chipStrokeWidth = 1f
        chip.chipStrokeColor = androidx.core.content.ContextCompat.getColorStateList(this, R.color.accent_blue)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.marginEnd = 8
        chip.layoutParams = layoutParams

        chip.setOnClickListener {
            navigateToUrl(entry.url)
        }

        return chip
    }

    /**
     * 显示快捷地址下拉列表（作为弹窗）。
     */
    private fun showShortcutsDropdown() {
        val policy = PolicyManager.getCurrentPolicy()
        val shortcuts = policy.shortcutUrls
        if (shortcuts.isEmpty()) {
            Toast.makeText(this, R.string.no_shortcuts, Toast.LENGTH_SHORT).show()
            return
        }

        // 使用 BottomSheetDialog 或简单的 AlertDialog 展示列表
        val items = shortcuts.map { "${it.name} — ${it.url}" }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.btn_shortcuts)
            .setItems(items) { _, which ->
                if (which < shortcuts.size) {
                    navigateToUrl(shortcuts[which].url)
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    // ========================= 管理入口（长按检测） =========================

    @SuppressLint("ClickableViewAccessibility")
    private fun setupLongPressDetection() {
        webView.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    longPressStartTime = System.currentTimeMillis()
                    longPressTriggered = false
                    false
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    if (!longPressTriggered &&
                        System.currentTimeMillis() - longPressStartTime > Constants.ADMIN_LONG_PRESS_MS
                    ) {
                        longPressTriggered = true
                        onLongPressDetected()
                        true
                    } else {
                        false
                    }
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    longPressStartTime = 0L
                    longPressTriggered = false
                    false
                }
                else -> false
            }
        }
    }

    /**
     * 长按触发管理入口。
     */
    private fun onLongPressDetected() {
        // 检查是否已完成首次设置
        if (!PasswordManager.isSetupCompleted(this)) {
            // 首次启动，跳转设置
            startActivity(android.content.Intent(this, FirstSetupActivity::class.java))
            return
        }

        // 弹出密码验证对话框
        showPasswordDialog()
    }

    private fun showPasswordDialog() {
        PasswordDialog(this).apply {
            setOnSuccessListener {
                // 密码验证成功，进入管理面板
                startActivity(
                    android.content.Intent(this@MainActivity, LocalManagementActivity::class.java)
                )
            }
            show()
        }
    }

    // ========================= 策略初始化与同步 =========================

    private fun getInitialPolicy(): Policy {
        return try {
            PolicyManager.getCurrentPolicy()
        } catch (e: Exception) {
            // 如果 PolicyManager 尚未初始化（首次启动），返回默认策略
            Policy.defaultPolicy()
        }
    }

    private fun initPolicyAndLoad() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 初始化 PolicyManager（加载本地策略）
                PolicyManager.init(applicationContext)
                // 尝试同步远程策略
                PolicyManager.refreshPolicy()
            } catch (e: Exception) {
                // 同步失败，使用本地策略
            }

            // 切回主线程加载首页
            launch(Dispatchers.Main) {
                val policy = PolicyManager.getCurrentPolicy()
                webView.updatePolicy(policy)

                // 检查策略是否有效
                if (!PolicyManager.isPolicyValid()) {
                    // 显示等待配置页面
                    val notConfiguredHtml = com.school.browser.webview.BlockedPageHelper.generateNotConfiguredPageHtml()
                    webView.loadDataWithBaseURL(null, notConfiguredHtml, "text/html", "UTF-8", null)
                    urlEditText.setText("等待策略配置")
                } else {
                    webView.loadHomepage()
                    urlEditText.setText(policy.homepage)
                }
                refreshShortcuts()
            }
        }
    }

    // ========================= 生命周期 =========================

    override fun onResume() {
        super.onResume()
        // 从管理面板返回时刷新策略
        if (::webView.isInitialized) {
            val policy = PolicyManager.getCurrentPolicy()
            webView.updatePolicy(policy)
            refreshShortcuts()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            // 确认退出对话框
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("退出浏览器")
                .setMessage("确定要退出学校浏览器吗？")
                .setPositiveButton("退出") { _, _ -> finish() }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}