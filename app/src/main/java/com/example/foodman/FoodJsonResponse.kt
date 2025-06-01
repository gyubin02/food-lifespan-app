package com.example.foodman

import com.google.gson.annotations.SerializedName

data class FoodJsonResponse(
    @SerializedName("I2570")
    val result: FoodJsonBody?
)

data class FoodJsonBody(
    @SerializedName("total_count")
    val totalCount: String?,

    val row: List<FoodItem>?,

    val RESULT: ResultInfo?
)

data class FoodItem(
    val PRDLST_NM: String?,         // 품목명
    val PRDT_NM: String?,           // 제품명
    val CMPNY_NM: String?,          // 회사명
    val BRCD_NO: String?,           // 바코드
    val HRNK_PRDLST_NM: String?     // 대분류 품목명 (예: 건포류)
)

data class ResultInfo(
    val MSG: String?,           // 응답 메시지
    val CODE: String?           // 응답 코드
)