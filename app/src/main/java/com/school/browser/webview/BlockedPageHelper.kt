package com.school.browser.webview

/**
 * 生成拦截提示页面的 HTML 内容。
 * 当用户访问被策略拦截的网址时，WebView 显示此页面。
 */
object BlockedPageHelper {

    /**
     * 生成中文拦截提示页的完整 HTML。
     *
     * @param blockedUrl 被拦截的 URL（可选，用于显示）
     * @param mode 当前策略模式（"whitelist" 或 "blacklist"）
     * @return HTML 字符串
     */
    fun generateBlockedPageHtml(blockedUrl: String = "", mode: String = "whitelist"): String {
        val message = when (mode) {
            "whitelist" -> "根据学校策略，该网址不在白名单范围内，访问已被阻止。"
            else -> "根据学校策略，该网址在黑名单范围内，访问已被阻止。"
        }

        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>访问被拦截</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background-color: #F5F5F5;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            text-align: center;
            background: white;
            border-radius: 16px;
            padding: 48px 32px;
            box-shadow: 0 2px 16px rgba(0,0,0,0.08);
            max-width: 400px;
            width: 100%;
        }
        .icon {
            font-size: 64px;
            margin-bottom: 24px;
        }
        h1 {
            font-size: 22px;
            color: #212121;
            margin-bottom: 16px;
            font-weight: 600;
        }
        p {
            font-size: 15px;
            color: #757575;
            line-height: 1.6;
            margin-bottom: 8px;
            word-break: break-all;
        }
        .url-text {
            font-size: 12px;
            color: #BDBDBD;
            word-break: break-all;
            margin-top: 16px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="icon">🔒</div>
        <h1>访问被拦截</h1>
        <p>$message</p>
        <p>如有疑问请联系学校信息中心。</p>
        <div class="url-text">${blockedUrl.ifBlank { "" }}</div>
    </div>
</body>
</html>
        """.trimIndent()
    }

    /**
     * 生成"等待远程配置"提示页的 HTML。
     * 当没有任何有效策略时显示此页面。
     */
    fun generateNotConfiguredPageHtml(): String {
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>等待策略配置</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background-color: #F5F5F5;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            text-align: center;
            background: white;
            border-radius: 16px;
            padding: 48px 32px;
            box-shadow: 0 2px 16px rgba(0,0,0,0.08);
            max-width: 440px;
            width: 100%;
        }
        .icon {
            font-size: 64px;
            margin-bottom: 24px;
        }
        h1 {
            font-size: 22px;
            color: #212121;
            margin-bottom: 16px;
            font-weight: 600;
        }
        p {
            font-size: 15px;
            color: #757575;
            line-height: 1.6;
            margin-bottom: 12px;
        }
        .steps {
            text-align: left;
            background: #F8F9FA;
            border-radius: 8px;
            padding: 16px 20px;
            margin: 16px 0;
        }
        .steps ol {
            margin: 0;
            padding-left: 20px;
            color: #616161;
            font-size: 14px;
            line-height: 2;
        }
        .file-path {
            font-family: monospace;
            background: #E8EAF6;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 13px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="icon">🔧</div>
        <h1>等待策略配置</h1>
        <p>浏览器尚未加载任何网址拦截策略，无法正常使用。</p>
        <p>请联系管理员完成以下任一配置：</p>
        <div class="steps">
            <ol>
                <li>配置<b>远程策略服务器</b>地址（HTTP/HTTPS 或 SMB）</li>
                <li>在 <span class="file-path">policy_data/true_website.txt</span> 中放置白名单域名文件</li>
                <li>在 <span class="file-path">policy_data/notrue_website.txt</span> 中放置黑名单域名文件</li>
            </ol>
        </div>
        <p style="font-size: 13px; color: #9E9E9E;">长按屏幕空白处 3 秒进入管理面板</p>
    </div>
</body>
</html>
        """.trimIndent()
    }
}