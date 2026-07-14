package com.school.browser.policy.manager

import android.content.Context
import com.school.browser.policy.local.LocalPolicyEditor
import com.school.browser.policy.model.Policy
import com.school.browser.policy.model.RemoteSourceConfig
import com.school.browser.sync.SyncScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 策略管理器（单例）—— 负责策略加载、缓存、优先级调度。
 *
 * 优先级：远程策略 > 缓存远程策略 > 本地文件 (.txt) > 本地加密策略
 *
 * 策略有效性判断：
 * - 白名单模式：whitelist 不为空 → 有效（至少有网址才能访问）
 * - 黑名单模式：始终有效（空黑名单 = 不拦截任何网址）
 */
object PolicyManager {

    private lateinit var appContext: Context
    private var currentPolicy: Policy = Policy.defaultPolicy()
    private var remoteSourceConfig: RemoteSourceConfig = RemoteSourceConfig.defaultConfig()

    /** 标记是否已初始化 */
    private var initialized = false

    /**
     * 初始化策略管理器（应用启动时调用一次）。
     */
    fun init(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        remoteSourceConfig = LocalPolicyEditor.loadRemoteConfig(appContext)
        currentPolicy = LocalPolicyEditor.load(appContext)
        initialized = true
    }

    /**
     * 获取当前生效的策略。
     */
    fun getCurrentPolicy(): Policy = currentPolicy

    /**
     * 获取远程源配置。
     */
    fun getRemoteConfig(): RemoteSourceConfig = remoteSourceConfig

    /**
     * 检查当前策略是否有效。
     *
     * 白名单模式：whitelist 不为空则有效
     * 黑名单模式：始终有效（黑名单为空 = 不拦截任何网址）
     *
     * @return true 表示策略有效，浏览器可正常使用
     */
    fun isPolicyValid(): Boolean {
        return when (currentPolicy.mode) {
            "whitelist" -> currentPolicy.whitelist.isNotEmpty()
            "blacklist" -> true // 黑名单模式：空列表 = 不拦截，有效
            else -> false
        }
    }

    /**
     * 刷新策略 —— 远程 > 缓存远程 > 本地文件 > 本地加密策略。
     */
    suspend fun refreshPolicy() {
        withContext(Dispatchers.IO) {
            val config = remoteSourceConfig

            // 1. 尝试远程拉取
            if (config.enabled && !config.forceLocalOnly) {
                val remotePolicy = try {
                    SyncScheduler.fetch(config)
                } catch (e: Exception) {
                    null
                }

                if (remotePolicy != null) {
                    LocalPolicyEditor.cacheRemotePolicy(appContext, remotePolicy)
                    currentPolicy = remotePolicy
                    return@withContext
                }

                // 2. 远程失败，尝试缓存远程策略
                val cached = LocalPolicyEditor.loadCachedRemotePolicy(appContext)
                if (cached != null) {
                    currentPolicy = cached
                    return@withContext
                }
            }

            // 3. 尝试从本地 .txt 文件读取
            val mode = currentPolicy.mode // 保持当前模式
            val filePolicy = LocalPolicyEditor.loadPolicyFromFile(appContext, mode)
            if (filePolicy != null) {
                currentPolicy = filePolicy
                return@withContext
            }

            // 4. 最终回退到本地加密策略
            currentPolicy = LocalPolicyEditor.load(appContext)
        }
    }

    /**
     * 保存本地策略（管理员手动修改后调用）。
     */
    fun saveLocalPolicy(policy: Policy) {
        currentPolicy = policy.copy(source = "local")
        LocalPolicyEditor.save(appContext, currentPolicy)
    }

    /**
     * 保存远程源配置。
     */
    fun saveRemoteConfig(config: RemoteSourceConfig) {
        remoteSourceConfig = config
        LocalPolicyEditor.saveRemoteConfig(appContext, config)
    }

    /**
     * 重置为默认策略（空策略）。
     */
    fun resetToDefault() {
        currentPolicy = Policy.defaultPolicy()
        LocalPolicyEditor.save(appContext, currentPolicy)
    }
}