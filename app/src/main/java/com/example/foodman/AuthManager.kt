package com.example.foodman

import com.google.firebase.auth.FirebaseAuth

object AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // 회원가입
    fun registerUser(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // 로그인
    fun loginUser(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // 로그아웃
    fun logout() {
        auth.signOut()
    }

    // 현재 로그인된 유저 UID 가져오기
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}