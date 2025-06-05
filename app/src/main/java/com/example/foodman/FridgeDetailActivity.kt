package com.example.foodman

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FridgeDetailActivity : AppCompatActivity() {
    private lateinit var fridgeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_detail)

        fridgeId = intent.getStringExtra("fridgeId") ?: run {
            Toast.makeText(this, "냉장고 ID가 없습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val title = intent.getStringExtra("title") ?: ""
        findViewById<TextView>(R.id.text_title).text = title

        loadIngredients()

        findViewById<ImageView>(R.id.btn_add_food).setOnClickListener {
            showFoodAddOptionPopup()
        }
    }

    private fun loadIngredients() {
        IngredientRepository.getAllIngredients(fridgeId) { ingredients ->
            val container = findViewById<LinearLayout>(R.id.food_container)
            container.removeAllViews()

            if (ingredients.isEmpty()) {
                Toast.makeText(this, "등록된 식재료가 없습니다", Toast.LENGTH_SHORT).show()
                return@getAllIngredients
            }

            val inflater = LayoutInflater.from(this)
            for ((_, ingredient) in ingredients) {
                val view = inflater.inflate(R.layout.item_food, container, false)
                view.findViewById<TextView>(R.id.text_food_name).text = ingredient.name
                view.findViewById<EditText>(R.id.edit_exp_date).setText("~${ingredient.expirationDate}")
                container.addView(view)
            }
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

        dialog.findViewById<LinearLayout>(R.id.scan_food_card).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("mode", "food")
            intent.putExtra("fridgeId", fridgeId)
            startActivity(intent)
        }

        dialog.findViewById<LinearLayout>(R.id.scan_barcode_card).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("mode", "barcode")
            intent.putExtra("fridgeId", fridgeId)
            startActivity(intent)
        }

        dialog.findViewById<LinearLayout>(R.id.scan_receipt_card).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("mode", "receipt")
            intent.putExtra("fridgeId", fridgeId)
            startActivity(intent)
        }

        dialog.show()
    }
}
