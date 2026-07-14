package com.school.browser.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.school.browser.R
import com.school.browser.security.PasswordManager
import com.school.browser.security.RecoveryHandler
import com.school.browser.util.Constants

/**
 * 密码恢复弹窗 —— 通过 TOTP 恢复码重置密码。
 *
 * 流程：
 * 1. 用户输入 6 位动态验证码
 * 2. 验证通过后，设置新密码
 * 3. 验证失败 5 次后锁定 30 分钟
 */
class RecoveryDialog(context: Context) : AlertDialog(context) {

    private var attempts = 0

    private lateinit var etRecoveryCode: EditText
    private lateinit var tvError: TextView
    private lateinit var btnConfirm: Button
    private lateinit var btnCancel: Button

    init {
        initDialog()
    }

    private fun initDialog() {
        val view = View.inflate(context, R.layout.dialog_recovery, null)
        setView(view)

        etRecoveryCode = view.findViewById(R.id.et_recovery_code)
        tvError = view.findViewById(R.id.tv_error)
        btnConfirm = view.findViewById(R.id.btn_confirm_recovery)
        btnCancel = view.findViewById(R.id.btn_cancel)

        btnConfirm.setOnClickListener { verifyRecoveryCode() }
        btnCancel.setOnClickListener { dismiss() }

        setTitle(R.string.recovery_title)
        setCancelable(false)
    }

    /**
     * 验证恢复码。
     */
    private fun verifyRecoveryCode() {
        if (attempts >= Constants.MAX_RECOVERY_ATTEMPTS) {
            Toast.makeText(context, R.string.recovery_locked, Toast.LENGTH_LONG).show()
            dismiss()
            return
        }

        val code = etRecoveryCode.text.toString().trim()

        if (code.length != 6) {
            tvError.visibility = View.VISIBLE
            tvError.text = "请输入6位验证码"
            return
        }

        if (RecoveryHandler.validate(context, code)) {
            // 验证成功，跳转设置新密码
            dismiss()
            showSetNewPasswordDialog()
        } else {
            // 验证失败
            attempts++
            val remaining = Constants.MAX_RECOVERY_ATTEMPTS - attempts
            if (remaining > 0) {
                tvError.visibility = View.VISIBLE
                tvError.text = "验证码无效，剩余 $remaining 次尝试"
                etRecoveryCode.text.clear()
            } else {
                // 超出最大尝试次数，锁定
                tvError.visibility = View.VISIBLE
                tvError.text = context.getString(R.string.recovery_locked)
                etRecoveryCode.isEnabled = false
                btnConfirm.isEnabled = false
                // 30分钟后自动关闭
                etRecoveryCode.postDelayed({ dismiss() }, 3000)
            }
        }
    }

    /**
     * 显示设置新密码的弹窗。
     */
    private fun showSetNewPasswordDialog() {
        val view = View.inflate(context, R.layout.dialog_set_new_password, null)
        val etNewPassword = view.findViewById<EditText>(R.id.et_new_password)
        val etConfirmPassword = view.findViewById<EditText>(R.id.et_confirm_password)

        AlertDialog.Builder(context)
            .setTitle(R.string.set_new_password)
            .setView(view)
            .setPositiveButton(R.string.btn_confirm) { dialog, _ ->
                val newPwd = etNewPassword.text.toString()
                val confirmPwd = etConfirmPassword.text.toString()

                if (newPwd.length < Constants.PASSWORD_MIN_LENGTH) {
                    Toast.makeText(context, "密码至少${Constants.PASSWORD_MIN_LENGTH}位", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPwd != confirmPwd) {
                    Toast.makeText(context, R.string.passwords_not_match, Toast.LENGTH_SHORT)
                        .show()
                    return@setPositiveButton
                }

                val hash = PasswordManager.hash(newPwd)
                PasswordManager.savePasswordHash(context, hash)
                Toast.makeText(context, R.string.password_changed, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
}