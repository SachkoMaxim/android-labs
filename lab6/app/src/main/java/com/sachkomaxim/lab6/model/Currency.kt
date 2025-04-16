package com.sachkomaxim.lab6.model

import com.google.gson.annotations.SerializedName

data class Currency(
    @SerializedName("r030") val r030: Int,
    @SerializedName("txt") val txt: String,
    @SerializedName("rate") val rate: Float,
    @SerializedName("cc") val cc: String,
    @SerializedName("exchangedate") val exchangeDate: String
)
