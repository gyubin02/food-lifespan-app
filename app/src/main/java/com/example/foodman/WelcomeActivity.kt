package com.example.foodman

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        try {
            // Create Account 버튼 클릭 시 SignUpActivity로 전환
            findViewById<Button>(R.id.btn_create_account).setOnClickListener {
                Log.d("WelcomeActivity", "Create Account 버튼 클릭")
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }

            // Login 버튼 클릭 시 LoginActivity로 전환
            findViewById<Button>(R.id.btn_login).setOnClickListener {
                Log.d("WelcomeActivity", "Login 버튼 클릭")
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e("WelcomeActivity", "오류 발생: ${e.message}")
            Toast.makeText(this, "화면 전환 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
} 