package com.sachkomaxim.lab3.database.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TravelRouteDao {

    @Insert
    suspend fun insert(travelRoute: TravelRoute): Long

    @Query("SELECT * FROM travel_routes ORDER BY timestamp DESC")
    suspend fun getAllTravelRoutes(): List<TravelRoute>

    @Query("DELETE FROM travel_routes")
    suspend fun deleteAllTravelRoutes(): Int

    @Query("SELECT COUNT(*) FROM travel_routes")
    suspend fun getCount(): Int
}
