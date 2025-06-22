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
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)
            // 최초 진입 시 HomeFragment 표시
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }

            findViewById<ImageView>(R.id.bottom_home).setOnClickListener {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
            findViewById<ImageView>(R.id.bottom_user).setOnClickListener {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment())
                    .commit()
            }



            /*loadFridgesFromFirestore()
            findViewById<ImageView>(R.id.fab_add).setOnClickListener { showAddPopup() }
            //@@@@@@@@@@@@@@수정@@@@@@@@@@@@@@
            // 하단 네비게이션 버튼 클릭 리스너 설정
            findViewById<ImageView>(R.id.bottom_home).setOnClickListener {
                removeAllFragments() // 홈 버튼: Fragment 모두 제거 → 기존 화면
            }
            findViewById<ImageView>(R.id.bottom_user).setOnClickListener {
                showFragment(ProfileFragment()) // 사람 버튼: UserFragment 띄우기
            }*/
            // (알람 버튼 추가 시 showFragment(AlarmFragment()) 등으로 가능)


            //수정@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

        } catch (e: Exception) {
            Log.e("MainActivity", "초기화 중 오류 발생: ${e.message}")
            Toast.makeText(this, "앱 초기화 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }





    /*private fun loadFridgesFromFirestore() {
        FridgeRepository.getAllFridges { fridgeList ->
            fridgeList.forEach { (fridgeId, fridge) ->
                addItemCard(fridgeId, fridge.name, fridge.type)
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
                        FridgeRepository.getAllFridges { fridgeList ->
                            val matched = fridgeList.find { it.second.name == name && it.second.type == type }
                            matched?.let { (fridgeId, _) ->
                                addItemCard(fridgeId, name, type)
                            }
                        }
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

    private fun addItemCard(fridgeId: String, name: String, type: String) {
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
            intent.putExtra("fridgeId", fridgeId)
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
    //수정@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // 뒤로가기 버튼 시 popBackStack됨
            .commit()
    }

    // 모든 Fragment 제거하고 기존 메인 UI(카드 목록 등)만 남기기
    private fun removeAllFragments() {
        val fm = supportFragmentManager
        for (i in 0 until fm.backStackEntryCount) {
            fm.popBackStack()
        }
        // 또는 아래도 가능:
        // supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }*/
}