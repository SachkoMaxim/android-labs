package com.sachkomaxim.lab6.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://bank.gov.ua/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val currencyApi: CurrencyApi = retrofit.create(CurrencyApi::class.java)
}
