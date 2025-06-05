package com.example.foodman

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // 로그인 상태 확인
        val uid = AuthManager.getCurrentUserId()
        if (uid != null) {
            Log.d("WelcomeActivity", "로그인 유지 상태: $uid")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val loginButton = findViewById<Button>(R.id.btn_login)
        val createAccountButton = findViewById<Button>(R.id.btn_create_account)

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        createAccountButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
