package com.example.foodman

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        try {
            val emailEdit = findViewById<EditText>(R.id.edit_email)
            val passwordEdit = findViewById<EditText>(R.id.edit_password)
            val loginButton = findViewById<Button>(R.id.login_button)
            val signupButton = findViewById<Button>(R.id.signup_button)

            loginButton.setOnClickListener {
                val email = emailEdit.text.toString().trim()
                val password = passwordEdit.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                AuthManager.loginUser(email, password) { success, error ->
                    if (success) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "로그인 실패: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            signupButton.setOnClickListener {
                startActivity(Intent(this, SignUpActivity::class.java))
            }

        } catch (e: Exception) {
            Log.e("LoginActivity", "초기화 중 오류 발생: ${e.message}")
            Toast.makeText(this, "앱 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
