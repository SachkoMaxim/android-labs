package com.sachkomaxim.lab3.database.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_routes")
data class TravelRoute(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val departure: String,
    val arrival: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis()
)
