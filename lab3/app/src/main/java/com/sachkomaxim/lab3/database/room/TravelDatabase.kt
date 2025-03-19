package com.sachkomaxim.lab3.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TravelRoute::class], version = 1, exportSchema = false)
abstract class TravelDatabase : RoomDatabase() {

    abstract fun travelRouteDao(): TravelRouteDao

    companion object {
        @Volatile
        private var INSTANCE: TravelDatabase? = null

        fun getDatabase(context: Context): TravelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelDatabase::class.java,
                    "travel_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
