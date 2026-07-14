package com.school.browser

import android.app.Application
import com.school.browser.policy.manager.PolicyManager

/**
 * Application 初始化入口。
 *
 * 在应用启动时初始化策略管理器，加载本地策略和远程源配置。
 */
class App : Application() {

    companion object {
        /** 全局 Application 实例，供需要 Context 的工具类使用 */
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化策略管理器（加载本地策略）
        PolicyManager.init(this)
    }
}