package com.sachkomaxim.lab3.database.room

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TravelRepository(context: Context) {

    private val travelRouteDao = TravelDatabase.getDatabase(context).travelRouteDao()

    suspend fun insertTravelRoute(departure: String, arrival: String, time: String): Long {
        return withContext(Dispatchers.IO) {
            travelRouteDao.insert(TravelRoute(
                departure = departure,
                arrival = arrival,
                time = time
            ))
        }
    }

    suspend fun getAllTravelRoutes(): List<TravelRoute> {
        return withContext(Dispatchers.IO) {
            travelRouteDao.getAllTravelRoutes()
        }
    }

    suspend fun clearAllTravelRoutes(): Int {
        return withContext(Dispatchers.IO) {
            travelRouteDao.deleteAllTravelRoutes()
        }
    }

    suspend fun hasTravelRoutes(): Boolean {
        return withContext(Dispatchers.IO) {
            travelRouteDao.getCount() > 0
        }
    }
}
