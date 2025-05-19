package com.example.foodman

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodBarcodeApi {
    @GET("api/{key}/{service}/json/{start}/{end}/")
    fun getFoodInfo(
        @Path("key") apiKey: String,
        @Path("service") serviceId: String,
        @Path("start") startIdx: Int,
        @Path("end") endIdx: Int,
        @Query("BRCD_NO") barcode: String
    ): Call<FoodJsonResponse>
}
