package com.sachkomaxim.lab5.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StepDataDao {
    @Insert
    suspend fun insertStepData(stepData: StepData)

    @Query("SELECT * FROM step_data ORDER BY timestamp DESC")
    suspend fun getAllStepData(): List<StepData>

    @Query("SELECT * FROM step_data WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    suspend fun getStepDataInRange(startTime: Long, endTime: Long): List<StepData>

    @Query("SELECT SUM(steps) FROM step_data WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getTotalStepsInRange(startTime: Long, endTime: Long): Int?

    @Query("DELETE FROM step_data")
    suspend fun deleteAllStepData()
}
