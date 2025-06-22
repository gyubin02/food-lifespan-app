package com.example.foodman
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class IngredientInputActivity : AppCompatActivity() {
    private lateinit var fridgeId: String
    private lateinit var resultMap: Map<String, String> // 이미지 분석 결과

    private fun formatAndValidateDate(input: String): String? {
        val formatted = when {
            Regex("""^\d{8}$""").matches(input) -> {
                // yyyymmdd 입력이면 yyyy-MM-dd로 자동 변환
                val year = input.substring(0, 4)
                val month = input.substring(4, 6)
                val day = input.substring(6, 8)
                "$year-$month-$day"
            }
            Regex("""^\d{4}-\d{2}-\d{2}$""").matches(input) -> input // 정상형식
            else -> return null // 형식 아님
        }

        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            sdf.isLenient = false
            val parsed = sdf.parse(formatted) ?: return null

            val today = sdf.parse(sdf.format(java.util.Date()))
            if (parsed.before(today)) {
                null // 이미 지난 날짜
            } else {
                formatted
            }
        } catch (e: Exception) {
            null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredient_input)

        // 1) Intent로부터 값 받기
        fridgeId = intent.getStringExtra("fridgeId") ?: ""
        val foodNames = intent.getStringArrayListExtra("food_names") ?: arrayListOf()
        val expirations = intent.getStringArrayListExtra("expirations") ?: arrayListOf()
        resultMap = foodNames.zip(expirations).toMap()   // Map 재구성!

        val container = findViewById<LinearLayout>(R.id.ingredient_container)
        val btnAddFood = findViewById<LinearLayout>(R.id.btn_add_food)
        val btnSave = findViewById<Button>(R.id.btn_save)

        // [1] 분석 결과 map으로 UI행 추가
        for ((name, expiration) in resultMap) {
            val rowView = LayoutInflater.from(this).inflate(R.layout.item_ingredient_input, container, false)
            rowView.findViewById<EditText>(R.id.edit_name).setText(name)
            rowView.findViewById<EditText>(R.id.edit_expiration).setText(expiration)
            container.addView(rowView)
        }

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // [2] "식재료 추가하기" 버튼
        btnAddFood.setOnClickListener {
            val rowView = LayoutInflater.from(this).inflate(R.layout.item_ingredient_input, container, false)
            container.addView(rowView)
        }

        // [3] 저장 버튼
        btnSave.setOnClickListener {
            val ingredientMap = mutableMapOf<String, Ingredient>()
            for (i in 0 until container.childCount) {
                val rowView = container.getChildAt(i)
                val name = rowView.findViewById<EditText>(R.id.edit_name).text.toString()
                val expiration = rowView.findViewById<EditText>(R.id.edit_expiration).text.toString()
                val extra = rowView.findViewById<EditText>(R.id.edit_extra).text.toString()

                if (name.isNotBlank() && expiration.isNotBlank()) {

                    val formattedDate = formatAndValidateDate(expiration)
                    if (formattedDate == null) {
                        Toast.makeText(this, "날짜입력이 올바르지 않습니다.\n예: 20250624 또는 2025-06-24", Toast.LENGTH_SHORT).show()
                        rowView.findViewById<EditText>(R.id.edit_expiration).requestFocus()
                        return@setOnClickListener
                    }



                    ingredientMap[name] = Ingredient(
                        name = name,
                        expirationDate = formattedDate,   //expirationdate->yyyy-mm-dd형태로 변환
                        category = extra // 기타 정보는 category에 임시 저장
                    )
                }
            }
            if (ingredientMap.isEmpty()) {
                Toast.makeText(this, "입력된 식재료가 없습니다.", Toast.LENGTH_SHORT).show()
                // finish()  // ← 만약 저장 없이 화면 닫고 싶으면 주석 해제
                return@setOnClickListener
            }



            // IngredientRepository로 저장
            var saveCount = 0
            var failCount = 0
            for ((_, ingredient) in ingredientMap) {
                IngredientRepository.addIngredient(fridgeId, ingredient) { success ->
                    if (success) {
                        // 유통기한 알림 예약
                        WorkManagerScheduler.scheduleExpirationAlerts(
                            context = this,
                            foodName = ingredient.name,
                            expirationDate = ingredient.expirationDate

                        )

                        saveCount++ }
                    else failCount++
                    if (saveCount + failCount == ingredientMap.size) {
                        if (failCount == 0)
                            Toast.makeText(this, "모든 식재료 저장 완료!", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(this, "일부 저장 실패", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }
}
