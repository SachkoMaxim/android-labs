package com.sachkomaxim.lab5.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_data")
data class StepData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val steps: Int
)
