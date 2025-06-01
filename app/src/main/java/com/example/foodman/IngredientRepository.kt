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
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    // 냉장고 ID에 따른 ingredients 컬렉션 참조
    private fun ingredientCollection(fridgeId: String) =
        uid?.let { db.collection("users").document(it).collection("fridges").document(fridgeId).collection("ingredients") }

    fun addIngredient(fridgeId: String, ingredient: Ingredient, onResult: (Boolean) -> Unit) {
        val now = Date()
        val data = ingredient.copy(createdAt = now, updatedAt = now)
        ingredientCollection(fridgeId)?.add(data)
            ?.addOnSuccessListener { onResult(true) }
            ?.addOnFailureListener { onResult(false) }
            ?: onResult(false)
    }

    fun getAllIngredients(fridgeId: String, onResult: (List<Pair<String, Ingredient>>) -> Unit) {
        ingredientCollection(fridgeId)
            ?.orderBy("expirationDate", Query.Direction.ASCENDING)
            ?.get()
            ?.addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    doc.toObject(Ingredient::class.java)?.let { doc.id to it }
                }
                onResult(list)
            }
            ?.addOnFailureListener {
                onResult(emptyList())
            }
            ?: onResult(emptyList())
    }

    fun updateIngredient(fridgeId: String, id: String, updated: Ingredient, onResult: (Boolean) -> Unit) {
        val data = updated.copy(updatedAt = Date())
        ingredientCollection(fridgeId)?.document(id)
            ?.set(data)
            ?.addOnSuccessListener { onResult(true) }
            ?.addOnFailureListener { onResult(false) }
            ?: onResult(false)
    }

    fun deleteIngredient(fridgeId: String, id: String, onResult: (Boolean) -> Unit) {
        ingredientCollection(fridgeId)?.document(id)
            ?.delete()
            ?.addOnSuccessListener { onResult(true) }
            ?.addOnFailureListener { onResult(false) }
            ?: onResult(false)
    }
}