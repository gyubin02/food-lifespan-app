package com.example.foodman

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            // add_circle 버튼 클릭 이벤트 설정
            findViewById<ImageView>(R.id.fab_add).setOnClickListener {
                showAddPopup()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "초기화 중 오류 발생: ${e.message}")
            Toast.makeText(this, "앱 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddPopup() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_popup)
        
        // Dialog의 배경을 투명하게 설정
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Dialog를 화면 전체에 표시
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        // Refrigerator 카드 클릭 이벤트
        dialog.findViewById<LinearLayout>(R.id.add_refri_card).setOnClickListener {
            dialog.dismiss()
            showNamePopup("refrigerator")
        }

        // Shelf 카드 클릭 이벤트
        dialog.findViewById<LinearLayout>(R.id.add_shelf_card).setOnClickListener {
            dialog.dismiss()
            showNamePopup("shelf")
        }
        
        dialog.show()
    }

    private fun showNamePopup(type: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_name_popup)
        
        // Dialog의 배경을 투명하게 설정
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Dialog를 화면 전체에 표시
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        // EditText와 TextView 참조
        val editText = dialog.findViewById<EditText>(R.id.edit_name)
        val textCount = dialog.findViewById<TextView>(R.id.text_length)

        // TextWatcher 설정
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 현재 입력된 텍스트의 길이를 표시
                textCount.text = "${s?.length ?: 0}/12"
            }
            
            override fun afterTextChanged(s: Editable?) {
                // 12자를 초과하는 경우 마지막 문자 제거
                if (s?.length ?: 0 > 12) {
                    s?.delete(12, s.length)
                }
            }
        })

        // Confirm 버튼 클릭 이벤트
        dialog.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            val name = editText.text.toString()
            if (name.isNotEmpty()) {
                addItemCard(name, type)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }

    private fun addItemCard(name: String, type: String) {
        // item_card.xml 레이아웃을 동적으로 생성
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_card, null)

        // 카드의 제목 설정
        cardView.findViewById<TextView>(R.id.title).text = name

        // 카드의 아이콘 설정
        val cardLayout = cardView.findViewById<LinearLayout>(R.id.item_card_layout)
        val iconView = cardLayout.getChildAt(0) as ImageView
        iconView.setImageResource(
            if (type == "refrigerator") R.drawable.add_refri
            else R.drawable.add_shelf
        )

        // 카드 클릭 이벤트 설정
        cardView.setOnClickListener {
            val intent = Intent(this, FridgeDetailActivity::class.java)
            intent.putExtra("title", name)
            startActivity(intent)
        }

        // 카드의 레이아웃 파라미터 설정
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 16) // 카드 간 간격 설정
        }

        // ScrollView 안의 LinearLayout에 카드 추가
        findViewById<LinearLayout>(R.id.container_layout).addView(cardView, layoutParams)
    }
}