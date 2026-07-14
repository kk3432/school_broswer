package com.school.browser.policy.fetcher

import com.school.browser.policy.model.Policy
import com.school.browser.policy.model.RemoteSourceConfig
import com.school.browser.security.CryptoUtil
import jcifs.context.SingletonContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * SMB 协议策略拉取器。
 *
 * 依赖 jcifs-ng 库连接 SMB/CIFS 共享文件夹，
 * 读取策略 JSON 文件内容并反序列化为 Policy 对象。
 */
class SmbPolicyFetcher : PolicyFetcher {

    override suspend fun fetch(config: RemoteSourceConfig): Policy = withContext(Dispatchers.IO) {
        val smbUrl = buildSmbUrl(config)
        val auth = NtlmPasswordAuthenticator(
            config.smbDomain.ifBlank { null },
            config.smbUser,
            decryptPassword(config.encryptedSmbPassword)
        )

        val smbFile = SmbFile(smbUrl, auth)
        smbFile.inputStream.use { inputStream ->
            val json = inputStream.bufferedReader().readText()
            Policy.fromJson(json)
        }
    }

    /**
     * 构建 SMB 文件 URL。
     * 格式：smb://host/share/path/file.json
     */
    private fun buildSmbUrl(config: RemoteSourceConfig): String {
        val host = config.smbHost.trim('/')
        val path = config.smbPath.trim('/')
        return "smb://$host/$path"
    }

    /**
     * 解密 SMB 密码（存储在配置中时已加密）。
     */
    private fun decryptPassword(encryptedPassword: String): String {
        return if (encryptedPassword.isBlank()) {
            ""
        } else {
            try {
                CryptoUtil.decrypt(encryptedPassword)
            } catch (e: Exception) {
                throw IOException("SMB 密码解密失败: ${e.message}")
            }
        }
    }
}