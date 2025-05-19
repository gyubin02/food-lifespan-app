package com.example.foodman

data class FoodJsonResponse(
    val C005: FoodJsonBody?
)

data class FoodJsonBody(
    val total_count: Int,
    val row: List<FoodItem>
)

data class FoodItem(
    val PRDLST_NM: String // 제품명
)
