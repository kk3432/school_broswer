package com.school.browser.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.school.browser.R
import com.school.browser.policy.local.LocalPolicyEditor
import com.school.browser.policy.model.Policy
import com.school.browser.security.PasswordManager
import com.school.browser.security.RecoveryHandler
import com.school.browser.util.Constants

/**
 * 首次设置引导页面。
 *
 * 流程：
 * 1. 用户输入管理密码两次确认
 * 2. 生成 TOTP 恢复种子并显示
 * 3. 用户确认后保存密码哈希和种子
 */
class FirstSetupActivity : AppCompatActivity() {

    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var tvSeedLabel: TextView
    private lateinit var tvSeedHint: TextView
    private lateinit var tvSeedValue: TextView
    private lateinit var btnComplete: Button

    private var generatedSeed: String = ""
    private var passwordSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_setup)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etPassword = findViewById(R.id.et_setup_password)
        etConfirmPassword = findViewById(R.id.et_setup_confirm_password)
        tvSeedLabel = findViewById(R.id.tv_seed_label)
        tvSeedHint = findViewById(R.id.tv_seed_hint)
        tvSeedValue = findViewById(R.id.tv_seed_value)
        btnComplete = findViewById(R.id.btn_complete_setup)
    }

    private fun setupListeners() {
        btnComplete.setOnClickListener {
            if (!passwordSaved) {
                savePassword()
            } else {
                // 密码和种子已保存，完成设置
                Toast.makeText(this, R.string.setup_complete, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * 保存密码，生成恢复种子。
     */
    private fun savePassword() {
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // 验证密码
        if (password.length < Constants.PASSWORD_MIN_LENGTH) {
            Toast.makeText(
                this,
                "密码至少需要 ${Constants.PASSWORD_MIN_LENGTH} 位",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, R.string.passwords_not_match, Toast.LENGTH_SHORT).show()
            return
        }

        // 保存密码哈希
        val hash = PasswordManager.hash(password)
        PasswordManager.savePasswordHash(this, hash)

        // 生成恢复种子
        generatedSeed = RecoveryHandler.generateSeed()
        RecoveryHandler.saveSeed(this, generatedSeed)

        // 保存默认策略
        LocalPolicyEditor.save(this, Policy.defaultPolicy())

        // 标记密码已保存，显示恢复种子
        passwordSaved = true
        showRecoverySeed()
    }

    /**
     * 显示恢复种子信息。
     */
    private fun showRecoverySeed() {
        // 隐藏密码输入框
        etPassword.isEnabled = false
        etConfirmPassword.isEnabled = false

        // 显示种子信息
        tvSeedLabel.visibility = View.VISIBLE
        tvSeedHint.visibility = View.VISIBLE
        tvSeedValue.visibility = View.VISIBLE
        tvSeedValue.text = generatedSeed

        // 修改按钮文字
        btnComplete.text = "我已保存，完成设置"
    }
}