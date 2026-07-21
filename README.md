# 学校受控浏览器 (School Controlled Browser)

一款面向学校场景的 Android 受控浏览器，基于系统 WebView，通过统一策略拦截网址访问，确保学生仅能访问教学相关网站。

---

## 功能特性

| 功能 | 描述 |
|------|------|
| **系统 WebView** | 强制使用 Android 系统 WebView，不内嵌第三方内核 |
| **黑白名单拦截** | 支持白名单模式（仅允许列表内网址）和黑名单模式（禁止列表内网址） |
| **通配符匹配** | 支持 `*.school.edu.cn` 形式的通配符域名匹配 |
| **进度条** | 页面加载时顶部线性进度条，类似 Chrome |
| **地址栏** | 显示当前 URL，支持输入导航，提交时自动校验策略 |
| **快捷地址** | 横向滚动标签 + 下拉列表弹窗，支持预设常用站点 |
| **远程策略拉取** | 支持 HTTP/HTTPS 和 SMB 协议拉取远程策略 JSON |
| **本地离线管理** | AES-256-GCM 加密存储本地策略，支持离线配置 |
| **策略优先级** | 远程策略 > 缓存远程策略 > 本地策略 > 默认策略 |
| **密码保护** | BCrypt 哈希 + EncryptedSharedPreferences，错误次数锁定 |
| **动态密码恢复** | TOTP 算法恢复（待实现），首次设置时生成恢复种子 |
| **防绕过加固** | 禁止文件访问/多窗口/下载、ProGuard 混淆、无地址栏输入自由输入 |

---

## 界面预览

```
┌─────────────────────────────┐
│        进度条 (2dp)          │  ← ProgressBar
├─────────────────────────────┤
│  🔒 [当前 URL 显示区域]  ⟳ ⭐│  ← 地址栏 + 刷新 + 快捷入口
├─────────────────────────────┤
│ [学校官网] [学习平台] [图书馆] │→ │  ← 快捷地址标签 (横向滚动)
├─────────────────────────────┤
│                             │
│       WebView (全屏)        │  ← 系统 WebView
│                             │
│                             │
└─────────────────────────────┘
    长按 3 秒 → 密码弹窗 → 管理面板
```

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 100% |
| UI | XML Layout + Material Components |
| 序列化 | kotlinx-serialization 1.6.2 |
| HTTP | OkHttp 4.12.0 |
| SMB | jcifs-ng 2.1.10 |
| 密码哈希 | BCrypt 0.10.2 (at.favre.lib) |
| 加密存储 | Android Keystore + EncryptedSharedPreferences 1.0.0 |
| 加密算法 | AES-256-GCM (AEAD) |
| 网络安全 | network_security_config.xml |
| 协程 | Kotlin Coroutines 1.7.3 |
| 混淆 | ProGuard + 完整序列化保留规则 |

---

## 项目结构

```
school_broswer/
├── build.gradle.kts                    # 项目级构建配置
├── settings.gradle.kts                 # 项目设置
├── gradle.properties                   # Gradle 属性
├── README.md
├── app/
│   ├── build.gradle.kts                # 模块构建配置
│   ├── proguard-rules.pro              # 混淆规则
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/school/browser/
│       │   ├── App.kt                  # Application 入口
│       │   ├── policy/
│       │   │   ├── model/
│       │   │   │   ├── Policy.kt              # 策略数据模型
│       │   │   │   └── RemoteSourceConfig.kt  # 远程源配置模型
│       │   │   ├── manager/
│       │   │   │   └── PolicyManager.kt       # 策略管理器 (单例)
│       │   │   ├── fetcher/
│       │   │   │   ├── PolicyFetcher.kt       # 拉取接口
│       │   │   │   ├── HttpPolicyFetcher.kt   # HTTP/HTTPS 实现
│       │   │   │   └── SmbPolicyFetcher.kt    # SMB 实现
│       │   │   └── local/
│       │   │       └── LocalPolicyEditor.kt   # 本地策略读写
│       │   ├── sync/
│       │   │   └── SyncScheduler.kt           # 同步调度器
│       │   ├── webview/
│       │   │   ├── ControlledWebView.kt       # WebView 安全封装
│       │   │   ├── InterceptingClient.kt      # 请求拦截器
│       │   │   └── BlockedPageHelper.kt       # 拦截提示页
│       │   ├── ui/
│       │   │   ├── MainActivity.kt            # 主界面 (全屏)
│       │   │   ├── FirstSetupActivity.kt      # 首次设置引导
│       │   │   ├── dialog/
│       │   │   │   ├── PasswordDialog.kt      # 密码验证弹窗
│       │   │   │   └── RecoveryDialog.kt      # 密码恢复弹窗
│       │   │   └── admin/
│       │   │       └── LocalManagementActivity.kt  # 管理面板
│       │   ├── security/
│       │   │   ├── CryptoUtil.kt              # AES 加解密
│       │   │   ├── PasswordManager.kt         # 密码哈希管理
│       │   │   └── RecoveryHandler.kt         # TOTP 恢复 (TODO)
│       │   └── util/
│       │       ├── Constants.kt               # 常量
│       │       ├── DomainMatcher.kt            # 域名通配符匹配
│       │       └── NetworkUtil.kt             # 网络状态检测
│       └── res/
│           ├── layout/                        # 8 个布局文件
│           ├── drawable/                      # 7 个资源文件
│           └── values/                        # strings, themes, colors
```

---

## 编译与运行

### 环境要求
- **Android Studio** Hedgehog (2023.1+) 或更高版本
- **JDK 17**
- **Android SDK 34**
- **Gradle 8.2**

### 编译步骤
1. 克隆仓库
   ```bash
   git clone https://github.com/kk3432/school_broswer.git
   cd school_broswer
   ```
2. 用 Android Studio 打开项目根目录
3. 等待 Gradle 同步完成
4. 连接 Android 设备（或启动模拟器，API 26+）
5. 点击 `Run` 或执行：
   ```bash
   ./gradlew assembleDebug
   ```

### 首次使用
1. 安装应用后启动，自动进入**首次设置**向导
2. 设置管理密码（至少 6 位）
3. **务必保存恢复种子**（用于忘记密码时恢复）
4. 完成设置后进入浏览器主页

---

## 策略 JSON 格式

远程策略服务器需返回以下格式的 JSON：

```json
{
  "version": 1,
  "mode": "whitelist",
  "whitelist": [
    "*.school.edu.cn",
    "www.school.edu.cn",
    "learning.example.com"
  ],
  "blacklist": [],
  "homepage": "https://www.school.edu.cn",
  "shortcutUrls": [
    { "name": "学校官网", "url": "https://www.school.edu.cn" },
    { "name": "学习平台", "url": "https://learning.school.edu.cn" }
  ],
  "source": "remote",
  "updatedAt": "2026-07-11T12:00:00Z"
}
```

---

## 远程管理入口

管理员在浏览器中**长按 WebView 空白处 3 秒**，弹出密码验证弹窗，验证通过后进入管理面板。

管理面板功能：
- **拦截模式**：白名单 / 黑名单切换
- **网址列表**：添加/删除域名
- **首页设置**：自定义默认首页
- **快捷地址**：添加/删除快捷站点
- **远程源配置**：HTTP/HTTPS 或 SMB 协议、跳过 SSL 验证、强制本地模式
- **安全设置**：修改密码、重置为默认策略

---

## 安全设计

| 数据 | 存储方式 | 加密手段 |
|------|----------|----------|
| 本地策略 | 内部存储文件 | AES-256-GCM (Android Keystore) |
| 远程策略缓存 | 内部存储文件 | AES-256-GCM |
| 管理密码 | EncryptedSharedPreferences | BCrypt 哈希 |
| 远程 SMB 密码 | EncryptedSharedPreferences | AES-256-GCM |
| TOTP 恢复种子 | 内部存储文件 | AES-256-GCM |
| 加密密钥 | Android Keystore | 硬件保护 (TEE/SE) |

### 防绕过清单
- [x] `allowFileAccess` = false
- [x] `allowContentAccess` = false
- [x] `allowFileAccessFromFileURLs` = false
- [x] `allowUniversalAccessFromFileURLs` = false
- [x] `setSupportMultipleWindows` = false
- [x] `shouldOverrideUrlLoading` 拦截非法导航
- [x] `shouldInterceptRequest` 拦截所有子资源
- [x] 禁止长按文本选择
- [x] 禁止下载和文件选择器
- [x] `isDebuggable` = false
- [x] ProGuard 代码混淆
- [x] 建议配合 MDM 单一应用模式

---

## TODO / 待办

- [ ] **TOTP 动态密码恢复** — `RecoveryHandler.kt` 中 `generateSeed()` 和 `validate()` 方法需实现真正的 TOTP 算法（推荐 Apache Commons Codec / kotlin-totp）
- [ ] **二维码生成** — 首次设置时将 TOTP 种子生成二维码方便扫码保存
- [ ] **策略签名校验** — 远程策略添加 HMAC 签名验证防止篡改
- [ ] **后台管理系统** — 独立的 Web 管理端，提供 `/api/policy/latest` 等接口
- [ ] **测试用例** — 域名匹配测试、远程拉取失败回退测试、密码恢复流程测试
- [ ] **CI/CD** — GitHub Actions 自动构建发布 APK

---

## 许可证

本项目仅用于教育目的。版权所有 © 2026

---

*界面与代码注释均使用简体中文，符合国内学校使用场景。*
