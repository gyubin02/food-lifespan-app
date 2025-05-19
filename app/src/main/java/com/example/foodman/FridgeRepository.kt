package com.example.foodman

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

// 냉장고/선반 정보 클래스
data class Fridge(
    val name: String = "",
    val type: String = "", // "refrigerator" 또는 "shelf"
    val createdAt: Date = Date()
)

object FridgeRepository {
    private val db = FirebaseFirestore.getInstance()
    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private fun fridgeCollection() =
        uid?.let { db.collection("users").document(it).collection("fridges") }

    // 냉장고/선반 추가
    fun addFridge(fridge: Fridge, onResult: (Boolean) -> Unit) {
        fridgeCollection()?.add(fridge)
            ?.addOnSuccessListener { onResult(true) }
            ?.addOnFailureListener { onResult(false) }
            ?: onResult(false)
    }

    // 전체 냉장고/선반 목록 조회
    fun getAllFridges(onResult: (List<Pair<String, Fridge>>) -> Unit) {
        fridgeCollection()
            ?.get()
            ?.addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    doc.toObject(Fridge::class.java)?.let { doc.id to it }
                }
                onResult(list)
            }
            ?.addOnFailureListener {
                onResult(emptyList())
            }
            ?: onResult(emptyList())
    }

    // 수정
    fun updateFridge(id: String, updated: Fridge, onResult: (Boolean) -> Unit) {
        fridgeCollection()?.document(id)
            ?.set(updated)
            ?.addOnSuccessListener { onResult(true) }
            ?.addOnFailureListener { onResult(false) }
            ?: onResult(false)
    }

    // 삭제
    fun deleteFridge(id: String, onResult: (Boolean) -> Unit) {
        fridgeCollection()?.document(id)
            ?.delete()
            ?.addOnSuccessListener { onResult(true) }
            ?.addOnFailureListener { onResult(false) }
            ?: onResult(false)
    }
}
