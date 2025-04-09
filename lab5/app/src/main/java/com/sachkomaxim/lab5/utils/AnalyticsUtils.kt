package com.sachkomaxim.lab5.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import kotlin.math.roundToInt

class AnalyticsUtils(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("analytics_prefs", Context.MODE_PRIVATE)

    private val defaultWeight = 85.0 // kg
    private val defaultHeight = 187.5 // cm
    private val defaultGender = "male"

    fun getUserWeight(): Double {
        return sharedPreferences.getFloat("user_weight", defaultWeight.toFloat()).toDouble()
    }

    fun getUserHeight(): Double {
        return sharedPreferences.getFloat("user_height", defaultHeight.toFloat()).toDouble()
    }

    fun getUserGender(): String {
        return sharedPreferences.getString("user_gender", defaultGender) ?: defaultGender
    }

    fun calculateStrideLength(): Double {
        val height = getUserHeight()
        return when (getUserGender()) {
            "male" -> height * 0.415 / 100 // in m
            "female" -> height * 0.413 / 100 // in m
            else -> height * 0.414 / 100
        }
    }

    fun calculateDistance(steps: Int): Double {
        val strideLength = calculateStrideLength()
        return (steps * strideLength / 1000).roundToDouble(2) // in km
    }

    fun calculateCalories(steps: Int): Int {
        val weight = getUserWeight()
        val distance = calculateDistance(steps) // in km

        // Calories burned formula: weight (kg) * distance (km) * constant factor
        // The constant factor varies, but 0.75 is a common approximation
        return (weight * distance * 0.75).roundToInt()
    }

    fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getEndOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun Double.roundToDouble(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (this * multiplier).roundToInt() / multiplier
    }
}