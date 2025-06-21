package com.example.foodman
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class IngredientInputActivity : AppCompatActivity() {
    private lateinit var fridgeId: String
    private lateinit var resultMap: Map<String, String> // 이미지 분석 결과

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
                    ingredientMap[name] = Ingredient(
                        name = name,
                        expirationDate = expiration,
                        category = extra // 기타 정보는 category에 임시 저장
                    )
                }
            }

            // IngredientRepository로 저장
            var saveCount = 0
            var failCount = 0
            for ((_, ingredient) in ingredientMap) {
                IngredientRepository.addIngredient(fridgeId, ingredient) { success ->
                    if (success) saveCount++ else failCount++
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
