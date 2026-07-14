package com.school.browser.sync

import com.school.browser.policy.fetcher.HttpPolicyFetcher
import com.school.browser.policy.fetcher.PolicyFetcher
import com.school.browser.policy.fetcher.SmbPolicyFetcher
import com.school.browser.policy.model.Policy
import com.school.browser.policy.model.RemoteSourceConfig
import com.school.browser.policy.model.RemoteType

/**
 * 策略同步调度器 —— 根据远程源配置类型分发到对应的拉取器。
 *
 * 支持 HTTP/HTTPS 和 SMB 两种协议。
 * 未来可以扩展更多协议（如 FTP、本地文件等）。
 */
object SyncScheduler {

    /**
     * 根据远程源配置执行策略拉取。
     *
     * @param config 远程源配置
     * @return 拉取到的 Policy 对象
     */
    suspend fun fetch(config: RemoteSourceConfig): Policy {
        val fetcher: PolicyFetcher = when (config.type) {
            RemoteType.HTTP -> HttpPolicyFetcher()
            RemoteType.SMB -> SmbPolicyFetcher()
        }
        return fetcher.fetch(config)
    }
}