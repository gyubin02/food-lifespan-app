package com.example.foodman

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface FoodBarcodeApi {
    @GET("api/{apiKey}/I2570/json/1/1/BRCD_NO={barcode}")
    fun getFoodInfo(
        @Path("apiKey") apiKey: String,
        @Path("barcode") barcode: String
    ): Call<FoodJsonResponse>
}