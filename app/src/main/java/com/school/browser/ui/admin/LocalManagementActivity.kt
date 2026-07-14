package com.school.browser.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.school.browser.R
import com.school.browser.policy.manager.PolicyManager
import com.school.browser.policy.model.Policy
import com.school.browser.policy.model.RemoteSourceConfig
import com.school.browser.policy.model.RemoteType
import com.school.browser.policy.model.ShortcutEntry
import com.school.browser.security.CryptoUtil
import com.school.browser.security.PasswordManager
import com.school.browser.util.Constants
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup

/**
 * 本地管理面板 —— 提供首页设置、快捷地址管理、远程源配置、安全设置。
 *
 * 注意：本地不再支持手动编辑白/黑名单，网址拦截策略必须来自远程服务器或本地 .txt 文件。
 */
class LocalManagementActivity : AppCompatActivity() {

    // ========== 首页设置 ==========
    private lateinit var etHomepage: EditText

    // ========== 快捷地址 ==========
    private lateinit var etShortcutName: EditText
    private lateinit var etShortcutUrl: EditText
    private lateinit var btnAddShortcut: Button
    private lateinit var rvShortcuts: RecyclerView
    private lateinit var tvNoShortcuts: TextView

    // ========== 远程源 ==========
    private lateinit var swRemoteEnabled: SwitchCompat
    private lateinit var swForceLocal: SwitchCompat
    private lateinit var rgRemoteType: RadioGroup
    private lateinit var rbHttp: RadioButton
    private lateinit var rbSmb: RadioButton
    private lateinit var etRemoteUrl: EditText
    private lateinit var swSkipSsl: SwitchCompat
    private lateinit var etSmbHost: EditText
    private lateinit var etSmbPath: EditText
    private lateinit var etSmbUser: EditText
    private lateinit var etSmbPassword: EditText

    // ========== 安全设置 ==========
    private lateinit var btnChangePassword: Button
    private lateinit var btnSave: Button

    // ========== 数据 ==========
    private var shortcutList: MutableList<ShortcutEntry> = mutableListOf()
    private var shortcutAdapter: ShortcutListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        title = getString(R.string.admin_panel_title)

        initViews()
        loadCurrentConfig()
        setupListeners()
    }

    private fun initViews() {
        // 首页
        etHomepage = findViewById(R.id.et_homepage)

        // 快捷地址
        etShortcutName = findViewById(R.id.et_shortcut_name)
        etShortcutUrl = findViewById(R.id.et_shortcut_url)
        btnAddShortcut = findViewById(R.id.btn_add_shortcut)
        rvShortcuts = findViewById(R.id.rv_shortcuts)
        tvNoShortcuts = findViewById(R.id.tv_no_shortcuts)

        // 远程源
        swRemoteEnabled = findViewById(R.id.sw_remote_enabled)
        swForceLocal = findViewById(R.id.sw_force_local)
        rgRemoteType = findViewById(R.id.rg_remote_type)
        rbHttp = findViewById(R.id.rb_http)
        rbSmb = findViewById(R.id.rb_smb)
        etRemoteUrl = findViewById(R.id.et_remote_url)
        swSkipSsl = findViewById(R.id.sw_skip_ssl)
        etSmbHost = findViewById(R.id.et_smb_host)
        etSmbPath = findViewById(R.id.et_smb_path)
        etSmbUser = findViewById(R.id.et_smb_user)
        etSmbPassword = findViewById(R.id.et_smb_password)

        // 安全设置
        btnChangePassword = findViewById(R.id.btn_change_password)
        btnSave = findViewById(R.id.btn_save)

        rvShortcuts.layoutManager = LinearLayoutManager(this)
    }

    private fun loadCurrentConfig() {
        val policy = PolicyManager.getCurrentPolicy()
        val remoteConfig = PolicyManager.getRemoteConfig()

        // 首页
        etHomepage.setText(policy.homepage)

        // 快捷地址
        shortcutList = policy.shortcutUrls.toMutableList()

        // 远程源
        swRemoteEnabled.isChecked = remoteConfig.enabled
        swForceLocal.isChecked = remoteConfig.forceLocalOnly
        if (remoteConfig.type == RemoteType.SMB) {
            rbSmb.isChecked = true
        } else {
            rbHttp.isChecked = true
        }
        etRemoteUrl.setText(remoteConfig.url)
        swSkipSsl.isChecked = remoteConfig.skipSslVerify
        etSmbHost.setText(remoteConfig.smbHost)
        etSmbPath.setText(remoteConfig.smbPath)
        etSmbUser.setText(remoteConfig.smbUser)
        // SMB 密码不直接回显（安全考虑）

        refreshShortcutList()
        updateSmbVisibility()
    }

    private fun setupListeners() {
        // 添加快捷地址
        btnAddShortcut.setOnClickListener {
            val name = etShortcutName.text.toString().trim()
            val url = etShortcutUrl.text.toString().trim()
            if (name.isBlank() || url.isBlank()) {
                Toast.makeText(this, "请输入名称和网址", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            shortcutList.add(ShortcutEntry(name, url))
            etShortcutName.text.clear()
            etShortcutUrl.text.clear()
            refreshShortcutList()
        }

        // 远程类型切换
        rgRemoteType.setOnCheckedChangeListener { _, _ -> updateSmbVisibility() }

        // 修改密码
        btnChangePassword.setOnClickListener { showChangePasswordDialog() }

        // 保存
        btnSave.setOnClickListener { saveAllChanges() }
    }

    private fun updateSmbVisibility() {
        val isSmb = rbSmb.isChecked
        val visibility = if (isSmb) View.VISIBLE else View.GONE
        etSmbHost.visibility = visibility
        etSmbPath.visibility = visibility
        etSmbUser.visibility = visibility
        etSmbPassword.visibility = visibility
    }

    // ========== 快捷地址 Adapter ==========

    private fun refreshShortcutList() {
        shortcutAdapter = ShortcutListAdapter(shortcutList) { position ->
            shortcutList.removeAt(position)
            refreshShortcutList()
        }
        rvShortcuts.adapter = shortcutAdapter
        tvNoShortcuts.visibility = if (shortcutList.isEmpty()) View.VISIBLE else View.GONE
    }

    private class ShortcutListAdapter(
        private val items: MutableList<ShortcutEntry>,
        private val onDelete: (Int) -> Unit
    ) : RecyclerView.Adapter<ShortcutListAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tv_shortcut_name)
            val tvUrl: TextView = itemView.findViewById(R.id.tv_shortcut_url)
            val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_shortcut, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = items[position]
            holder.tvName.text = entry.name
            holder.tvUrl.text = entry.url
            holder.btnDelete.setOnClickListener { onDelete(position) }
        }

        override fun getItemCount(): Int = items.size
    }

    // ========== 保存 ==========

    private fun saveAllChanges() {
        try {
            // 构建新策略（白名单/黑名单保持当前值，不再本地编辑）
            val currentPolicy = PolicyManager.getCurrentPolicy()
            val newPolicy = Policy(
                version = currentPolicy.version + 1,
                mode = currentPolicy.mode,
                whitelist = currentPolicy.whitelist,
                blacklist = currentPolicy.blacklist,
                homepage = etHomepage.text.toString().trim().ifBlank { Policy.defaultPolicy().homepage },
                shortcutUrls = shortcutList.toList(),
                source = "local"
            )

            // 构建远程源配置
            val remoteConfig = RemoteSourceConfig(
                type = if (rbSmb.isChecked) RemoteType.SMB else RemoteType.HTTP,
                url = etRemoteUrl.text.toString().trim(),
                enabled = swRemoteEnabled.isChecked,
                skipSslVerify = swSkipSsl.isChecked,
                smbHost = etSmbHost.text.toString().trim(),
                smbPath = etSmbPath.text.toString().trim(),
                smbUser = etSmbUser.text.toString().trim(),
                encryptedSmbPassword = if (etSmbPassword.text.toString().isNotBlank()) {
                    CryptoUtil.encrypt(etSmbPassword.text.toString())
                } else {
                    PolicyManager.getRemoteConfig().encryptedSmbPassword
                },
                forceLocalOnly = swForceLocal.isChecked
            )

            PolicyManager.saveLocalPolicy(newPolicy)
            PolicyManager.saveRemoteConfig(remoteConfig)

            Toast.makeText(this, R.string.policy_saved, Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChangePasswordDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)
        val etOldPassword = view.findViewById<EditText>(R.id.et_old_password)
        val etNewPassword = view.findViewById<EditText>(R.id.et_new_password)
        val etConfirmPassword = view.findViewById<EditText>(R.id.et_confirm_password)

        AlertDialog.Builder(this)
            .setTitle(R.string.change_password)
            .setView(view)
            .setPositiveButton(R.string.btn_confirm) { _, _ ->
                val oldPwd = etOldPassword.text.toString()
                val newPwd = etNewPassword.text.toString()
                val confirmPwd = etConfirmPassword.text.toString()

                if (newPwd.length < Constants.PASSWORD_MIN_LENGTH) {
                    Toast.makeText(this, "新密码至少${Constants.PASSWORD_MIN_LENGTH}位", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPwd != confirmPwd) {
                    Toast.makeText(this, R.string.passwords_not_match, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val success = PasswordManager.changePassword(this, oldPwd, newPwd)
                if (success) {
                    Toast.makeText(this, R.string.password_changed, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "当前密码错误", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
}