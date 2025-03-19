package com.sachkomaxim.lab3.database.sqlite

import android.provider.BaseColumns

object TravelContract {
    // Table contents are grouped together in an anonymous object.
    object TravelEntry : BaseColumns {
        const val TABLE_NAME = "travel_routes"
        const val COLUMN_DEPARTURE = "departure"
        const val COLUMN_ARRIVAL = "arrival"
        const val COLUMN_TIME = "time"
        const val COLUMN_TIMESTAMP = "timestamp"
    }
}
