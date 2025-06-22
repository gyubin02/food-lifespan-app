package com.example.foodman



import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 1. 사용자 ID 표시
        //val userId = AuthManager.getCurrentUserId()
        val email = FirebaseAuth.getInstance().currentUser?.email
        val tvUserId = view.findViewById<TextView>(R.id.tv_user_id)
        tvUserId.text = if (email != null) "$email 님" else "로그인 정보 없음"

        // 2. 로그아웃 버튼 처리
        view.findViewById<Button>(R.id.btn_logout).setOnClickListener {
            AuthManager.logout()
            Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

            // 로그인 화면으로 이동 (Activity에서 Fragment로 이동이므로, Activity 전체를 종료/변경)
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }
}
