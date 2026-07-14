package com.school.browser.util

/**
 * 全局常量定义
 */
object Constants {

    /** 应用包名 */
    const val PACKAGE_NAME = "com.school.browser"

    // ==================== 存储 Key ====================

    /** EncryptedSharedPreferences 文件名 */
    const val PREFS_NAME = "school_browser_secure_prefs"

    /** 管理密码哈希存储 Key */
    const val KEY_PASSWORD_HASH = "admin_password_hash"

    /** 远程源配置 JSON 存储 Key */
    const val KEY_REMOTE_CONFIG = "remote_source_config"

    /** 加密后的本地策略 JSON 存储 Key */
    const val KEY_ENCRYPTED_POLICY = "encrypted_local_policy"

    /** 加密后的远程策略缓存 JSON 存储 Key */
    const val KEY_CACHED_REMOTE_POLICY = "cached_remote_policy"

    /** 首次设置完成标记 */
    const val KEY_SETUP_COMPLETED = "first_setup_completed"

    /** TOTP 恢复种子加密存储 Key */
    const val KEY_RECOVERY_SEED = "encrypted_recovery_seed"

    /** 密码错误锁定时间戳 Key */
    const val KEY_PASSWORD_LOCK_TIME = "password_lock_timestamp"

    // ==================== 文件路径 ====================

    /** 本地策略文件存储目录（内部存储） */
    const val POLICY_DIR = "policy_data"

    /** 本地策略文件名 */
    const val LOCAL_POLICY_FILE = "local_policy.dat"

    /** 远程策略缓存文件名 */
    const val REMOTE_POLICY_CACHE_FILE = "remote_policy_cache.dat"

    /** 恢复种子文件名 */
    const val RECOVERY_SEED_FILE = "recovery_seed.dat"

    // ==================== 策略拉取 ====================

    /** HTTP 请求超时时间（秒） */
    const val HTTP_TIMEOUT_SECONDS = 15L

    /** 策略同步最小间隔（分钟） */
    const val SYNC_MIN_INTERVAL_MINUTES = 5L

    // ==================== 密码相关 ====================

    /** 密码最小长度 */
    const val PASSWORD_MIN_LENGTH = 6

    /** 密码最大错误次数 */
    const val MAX_PASSWORD_ATTEMPTS = 3

    /** 密码错误锁定时间（分钟） */
    const val PASSWORD_LOCK_MINUTES = 30L

    /** 恢复码最大尝试次数 */
    const val MAX_RECOVERY_ATTEMPTS = 5

    /** TOTP 验证窗口大小（前后各允许偏差的单位数） */
    const val TOTP_WINDOW_SIZE = 1

    // ==================== UI 相关 ====================

    /** 管理入口长按触发时间（毫秒） */
    const val ADMIN_LONG_PRESS_MS = 3000L

    /** 进度条动画时长（毫秒） */
    const val PROGRESS_ANIM_DURATION = 300L

    /** 进度条最大进度值（页面加载完成前保持在此值以下） */
    const val PROGRESS_MAX_BEFORE_FINISH = 90

    /** 快捷地址最大显示数量（地址栏下方） */
    const val MAX_SHORTCUT_DISPLAY = 6
}