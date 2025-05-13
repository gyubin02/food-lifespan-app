package com.example.foodman

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

// 데이터 클래스
data class Ingredient(
    val name: String = "",
    val category: String = "",
    val storage: String = "", // 냉장, 냉동, 상온 등
    val purchaseDate: String = "",
    val expirationDate: String = "",
    val imageUrl: String = "",
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)

object IngredientRepository {

    private val db = FirebaseFirestore.getInstance()
    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private fun ingredientCollection() =
        uid?.let {
            db.collection("users").document(it).collection("ingredients")
        }

    // 추가
    fun addIngredient(ingredient: Ingredient, onResult: (Boolean) -> Unit) {
        val data = ingredient.copy(
            createdAt = Date(),
            updatedAt = Date()
        )
        ingredientCollection()?.add(data)
            ?.addOnSuccessListener { onResult(true) }
            ?.addOnFailureListener { onResult(false) }
    }

    // 조회
    fun getAllIngredients(onResult: (List<Pair<String, Ingredient>>) -> Unit) {
        ingredientCollection()
            ?.orderBy("expirationDate", Query.Direction.ASCENDING)
            ?.get()
            ?.addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    val ing = doc.toObject(Ingredient::class.java)
                    if (ing != null) doc.id to ing else null
                }
                onResult(list)
            }
            ?.addOnFailureListener {
                onResult(emptyList())
            }
    }

    // 수정
    fun updateIngredient(id: String, updated: Ingredient, onResult: (Boolean) -> Unit) {
        val data = updated.copy(updatedAt = Date())
        ingredientCollection()?.document(id)
            ?.set(data)
            ?.addOnSuccessListener { onResult(true) }
            ?.addOnFailureListener { onResult(false) }
    }

    // 삭제
    fun deleteIngredient(id: String, onResult: (Boolean) -> Unit) {
        ingredientCollection()?.document(id)
            ?.delete()
            ?.addOnSuccessListener { onResult(true) }
            ?.addOnFailureListener { onResult(false) }
    }
}