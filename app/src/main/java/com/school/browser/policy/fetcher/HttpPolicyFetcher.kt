package com.school.browser.policy.fetcher

import com.school.browser.policy.model.Policy
import com.school.browser.policy.model.RemoteSourceConfig
import com.school.browser.util.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * HTTP/HTTPS 策略拉取器。
 *
 * 支持：
 * - 标准 HTTPS（证书验证）
 * - 跳过 SSL 证书验证（仅用于内网自签证书场景）
 * - 超时控制
 */
class HttpPolicyFetcher : PolicyFetcher {

    override suspend fun fetch(config: RemoteSourceConfig): Policy {
        val client = buildClient(config)
        val request = Request.Builder()
            .url(config.url)
            .header("Accept", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string()
            ?: throw IOException("远程服务器返回空响应")

        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.message}")
        }

        return Policy.fromJson(body)
    }

    /**
     * 构建 OkHttpClient，根据配置决定是否跳过 SSL 验证。
     */
    private fun buildClient(config: RemoteSourceConfig): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(Constants.HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (config.skipSslVerify) {
            val trustAllCerts = arrayOf<TrustManager>(TrustAllCertificates())
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())

            builder.sslSocketFactory(
                sslContext.socketFactory,
                trustAllCerts[0] as X509TrustManager
            )
            builder.hostnameVerifier { _, _ -> true }
        }

        return builder.build()
    }

    /**
     * 信任所有证书的 TrustManager（仅用于可信内网环境）。
     *
     * 安全警告：跳过 SSL 验证会使 HTTPS 连接失去安全保护，
     * 仅在完全可信的内网环境中使用此选项。
     */
    private class TrustAllCertificates : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }
}