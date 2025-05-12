package com.example.foodman

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FridgeDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_detail)

        // 전달받은 title 값 가져오기
        val title = intent.getStringExtra("title") ?: ""

        // title 값 설정
        findViewById<TextView>(R.id.text_title).text = title

        // 우측 하단 + 버튼 클릭 이벤트
        findViewById<ImageView>(R.id.btn_add_food).setOnClickListener {
            showFoodAddOptionPopup()
        }
    }

    private fun showFoodAddOptionPopup() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.food_add_option_popup)
        
        // Dialog의 배경을 투명하게 설정
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Dialog를 화면 전체에 표시
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        // Scan Food 버튼 클릭 이벤트
        dialog.findViewById<LinearLayout>(R.id.scan_food_card).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, CameraActivity::class.java))
        }

        // Scan Receipt 버튼 클릭 이벤트
        dialog.findViewById<LinearLayout>(R.id.scan_receipt_card).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, CameraActivity::class.java))
        }
        
        dialog.show()
    }
} 