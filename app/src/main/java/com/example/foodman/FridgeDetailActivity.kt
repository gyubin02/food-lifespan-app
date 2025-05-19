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

        val title = intent.getStringExtra("title") ?: ""
        findViewById<TextView>(R.id.text_title).text = title

        findViewById<ImageView>(R.id.btn_add_food).setOnClickListener {
            showFoodAddOptionPopup()
        }
    }

    private fun showFoodAddOptionPopup() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.food_add_option_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        // Scan Food 버튼 클릭 이벤트
        dialog.findViewById<LinearLayout>(R.id.scan_food_card).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("mode", "food")
            startActivity(intent)
        }

        // Scan Barcode 버튼 클릭 이벤트
        dialog.findViewById<LinearLayout>(R.id.scan_barcode_card).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("mode", "barcode")
            startActivity(intent)
        }

        // Scan Receipt 버튼 클릭 이벤트
        dialog.findViewById<LinearLayout>(R.id.scan_receipt_card).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("mode", "receipt")
            startActivity(intent)
        }

        dialog.show()
    }
}
