package com.example.foodman

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        val emailEdit = findViewById<EditText>(R.id.edit_email)
        val passwordEdit = findViewById<EditText>(R.id.edit_password)
        val confirmPasswordEdit = findViewById<EditText>(R.id.edit_confirm_password)
        val signupButton = findViewById<Button>(R.id.signup_button)

        signupButton.setOnClickListener {
            val email = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()
            val confirmPassword = confirmPasswordEdit.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AuthManager.registerUser(email, password) { success, error ->
                if (success) {
                    val uid = AuthManager.getCurrentUserId()
                    if (uid != null) {
                        val profile = UserProfile(
                            name = "사용자",  // 기본 이름 지정 또는 이후에 설정화면에서 변경
                            refrigeratorName = "냉장고",
                            shelfName = "선반",
                            alarmEnabled = true
                        )
                        UserRepository.createUserProfile(profile) { profileSaved ->
                            if (profileSaved) {
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "프로필 저장 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "회원가입 실패: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}