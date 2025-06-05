package com.example.foodman

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class UserProfile(
    val name: String = "",
    val refrigeratorName: String = "",
    val shelfName: String = "",
    val alarmEnabled: Boolean = true
)

object UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun createUserProfile(profile: UserProfile, onResult: (Boolean) -> Unit) {
        uid?.let {
            db.collection("users").document(it)
                .set(profile)
                .addOnSuccessListener {
                    Log.d("UserRepository", "사용자 프로필 등록 성공")
                    onResult(true)
                }
                .addOnFailureListener {
                    Log.e("UserRepository", "사용자 프로필 등록 실패: ${it.message}")
                    onResult(false)
                }
        } ?: onResult(false)
    }

    fun getUserProfile(onResult: (UserProfile?) -> Unit) {
        uid?.let {
            db.collection("users").document(it)
                .get()
                .addOnSuccessListener { doc ->
                    val profile = doc.toObject(UserProfile::class.java)
                    onResult(profile)
                }
                .addOnFailureListener {
                    Log.e("UserRepository", "사용자 정보 불러오기 실패: ${it.message}")
                    onResult(null)
                }
        } ?: onResult(null)
    }

    fun updateUserProfile(profile: UserProfile, onResult: (Boolean) -> Unit) {
        uid?.let {
            db.collection("users").document(it)
                .set(profile)
                .addOnSuccessListener {
                    Log.d("UserRepository", "사용자 프로필 업데이트 성공")
                    onResult(true)
                }
                .addOnFailureListener {
                    Log.e("UserRepository", "사용자 프로필 업데이트 실패: ${it.message}")
                    onResult(false)
                }
        } ?: onResult(false)
    }
}