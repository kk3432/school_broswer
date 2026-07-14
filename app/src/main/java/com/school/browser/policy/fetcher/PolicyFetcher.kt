package com.school.browser.policy.fetcher

import com.school.browser.policy.model.Policy
import com.school.browser.policy.model.RemoteSourceConfig

/**
 * 策略拉取接口 —— 定义从不同来源获取策略的统一契约。
 */
interface PolicyFetcher {
    /**
     * 从远程源拉取策略。
     *
     * @param config 远程源配置
     * @return 拉取到的 Policy 对象
     */
    suspend fun fetch(config: RemoteSourceConfig): Policy
}