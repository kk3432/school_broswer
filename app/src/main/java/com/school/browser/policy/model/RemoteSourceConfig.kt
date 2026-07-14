package com.school.browser.policy.model

import kotlinx.serialization.Serializable

/**
 * 远程策略源类型
 */
enum class RemoteType {
    HTTP,
    SMB
}

/**
 * 远程策略源配置
 *
 * @property type 协议类型
 * @property url HTTP/HTTPS 远程地址
 * @property enabled 是否启用远程拉取
 * @property skipSslVerify 是否跳过 SSL 证书验证（仅 HTTPS）
 * @property smbHost SMB 服务器地址
 * @property smbPath SMB 共享文件路径
 * @property smbDomain SMB 域
 * @property smbUser SMB 用户名
 * @property encryptedSmbPassword 已加密的 SMB 密码
 * @property forceLocalOnly 是否强制使用本地策略（忽略远程）
 */
@Serializable
data class RemoteSourceConfig(
    val type: RemoteType = RemoteType.HTTP,
    val url: String = "",
    val enabled: Boolean = false,
    val skipSslVerify: Boolean = false,
    val smbHost: String = "",
    val smbPath: String = "",
    val smbDomain: String = "",
    val smbUser: String = "",
    val encryptedSmbPassword: String = "",
    val forceLocalOnly: Boolean = true
) {
    companion object {
        /** 创建默认配置（仅本地模式） */
        fun defaultConfig(): RemoteSourceConfig = RemoteSourceConfig(
            enabled = false,
            forceLocalOnly = true
        )
    }
}