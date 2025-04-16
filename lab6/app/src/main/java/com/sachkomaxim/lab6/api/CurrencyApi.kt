package com.sachkomaxim.lab6.api

import com.sachkomaxim.lab6.model.Currency
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {
    @GET("NBUStatService/v1/statdirectory/exchange")
    fun getCurrency(
        @Query("valcode") currencyCode: String,
        @Query("date") date: String? = null,
        @Query("json") json: String = "true"
    ): Call<List<Currency>>
}
