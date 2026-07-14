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
import com.school.browser.util.Constants

/**
 * 管理密码验证弹窗。
 *
 * - 输入管理密码进行验证
 * - 错误次数达到上限后显示"忘记密码"按钮
 * - 错误次数超限后锁定
 */
class PasswordDialog(context: Context) : AlertDialog(context) {

    private var attempts = 0
    private var onSuccessListener: (() -> Unit)? = null

    private lateinit var etPassword: EditText
    private lateinit var btnConfirm: Button
    private lateinit var btnForgotPassword: Button

    init {
        initDialog()
    }

    private fun initDialog() {
        val view = View.inflate(context, R.layout.dialog_password, null)
        setView(view)

        etPassword = view.findViewById(R.id.et_password)
        btnConfirm = view.findViewById(R.id.btn_confirm)
        btnForgotPassword = view.findViewById(R.id.btn_forgot_password)

        btnConfirm.setOnClickListener { verifyPassword() }
        btnForgotPassword.setOnClickListener {
            dismiss()
            RecoveryDialog(context).show()
        }

        // 初始隐藏忘记密码按钮
        btnForgotPassword.visibility = View.GONE

        setTitle(R.string.enter_password_title)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    /**
     * 验证输入的密码。
     */
    private fun verifyPassword() {
        if (PasswordManager.isLocked(context)) {
            val remainingMs = PasswordManager.getRemainingLockTime(context)
            val remainingMinutes = (remainingMs / 60000) + 1
            Toast.makeText(
                context,
                "已锁定，请 ${remainingMinutes} 分钟后再试",
                Toast.LENGTH_LONG
            ).show()
            dismiss()
            return
        }

        val input = etPassword.text.toString()
        if (input.isBlank()) {
            Toast.makeText(context, "请输入密码", Toast.LENGTH_SHORT).show()
            return
        }

        if (PasswordManager.verify(context, input)) {
            // 密码正确
            attempts = 0
            onSuccessListener?.invoke()
            dismiss()
        } else {
            // 密码错误
            attempts++
            val remaining = Constants.MAX_PASSWORD_ATTEMPTS - attempts
            if (remaining > 0) {
                Toast.makeText(
                    context,
                    context.getString(R.string.attempts_remaining, remaining),
                    Toast.LENGTH_SHORT
                ).show()
                // 达到错误次数阈值后显示忘记密码按钮
                if (attempts >= Constants.MAX_PASSWORD_ATTEMPTS) {
                    btnForgotPassword.visibility = View.VISIBLE
                }
            } else {
                // 超出最大尝试次数，锁定
                PasswordManager.lock(context)
                Toast.makeText(context, R.string.password_locked, Toast.LENGTH_LONG).show()
                dismiss()
            }
            etPassword.text.clear()
        }
    }

    /**
     * 设置密码验证成功后的回调。
     */
    fun setOnSuccessListener(listener: () -> Unit) {
        onSuccessListener = listener
    }
}