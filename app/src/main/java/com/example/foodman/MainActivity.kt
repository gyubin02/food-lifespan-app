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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)
            //테스트
            //testImageAnalysisRepository()
            loadFridgesFromFirestore() // Firebase에서 냉장고 목록 불러오기

            findViewById<ImageView>(R.id.fab_add).setOnClickListener {
                showAddPopup()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "초기화 중 오류 발생: ${e.message}")
            Toast.makeText(this, "앱 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFridgesFromFirestore() {
        FridgeRepository.getAllFridges { fridgeList ->
            fridgeList.forEach { (_, fridge) ->
                addItemCard(fridge.name, fridge.type)
            }
        }
    }

    private fun showAddPopup() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        dialog.findViewById<LinearLayout>(R.id.add_refri_card).setOnClickListener {
            dialog.dismiss()
            showNamePopup("refrigerator")
        }

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
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        val editText = dialog.findViewById<EditText>(R.id.edit_name)
        val textCount = dialog.findViewById<TextView>(R.id.text_length)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textCount.text = "${s?.length ?: 0}/12"
            }
            override fun afterTextChanged(s: Editable?) {
                if (s?.length ?: 0 > 12) {
                    s?.delete(12, s.length)
                }
            }
        })

        dialog.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            val name = editText.text.toString()
            if (name.isNotEmpty()) {
                val fridge = Fridge(name = name, type = type)
                FridgeRepository.addFridge(fridge) { success ->
                    if (success) {
                        addItemCard(name, type)
                        Toast.makeText(this, "$type 추가됨", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "저장 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun addItemCard(name: String, type: String) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_card, null)

        cardView.findViewById<TextView>(R.id.title).text = name

        val cardLayout = cardView.findViewById<LinearLayout>(R.id.item_card_layout)
        val iconView = cardLayout.getChildAt(0) as ImageView
        iconView.setImageResource(
            if (type == "refrigerator") R.drawable.add_refri else R.drawable.add_shelf
        )

        cardView.setOnClickListener {
            val intent = Intent(this, FridgeDetailActivity::class.java)
            intent.putExtra("title", name)
            startActivity(intent)
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 16)
        }

        findViewById<LinearLayout>(R.id.container_layout).addView(cardView, layoutParams)
    }

    //테스트

    private fun testImageAnalysisRepository() {
        val base64Image = ImageBase64Util.base64FromAssetJpg(this, "image_test2.jpg", withPrefix = true) // 테스트용 이미지 문자열

        lifecycleScope.launch {
            try {
                if(base64Image!=null){
                val result = ImageAnalysisRepository.analyzeImageAndGetExpiryDateMap(base64Image)
                Log.d("GeminiTest", "결과: $result")}
            } catch (e: Exception) {
                Log.e("GeminiTest", "에러: ${e.message}", e)
            }
        }
    }

}
