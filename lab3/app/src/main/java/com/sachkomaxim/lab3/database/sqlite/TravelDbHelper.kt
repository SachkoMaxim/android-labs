package com.sachkomaxim.lab3.database.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.sachkomaxim.lab3.database.sqlite.TravelContract.TravelEntry

class TravelDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "TravelRoutes.db"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${TravelEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${TravelEntry.COLUMN_DEPARTURE} TEXT," +
                    "${TravelEntry.COLUMN_ARRIVAL} TEXT," +
                    "${TravelEntry.COLUMN_TIME} TEXT," +
                    "${TravelEntry.COLUMN_TIMESTAMP} INTEGER)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${TravelEntry.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun insertTravelRoute(departure: String, arrival: String, time: String): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(TravelEntry.COLUMN_DEPARTURE, departure)
            put(TravelEntry.COLUMN_ARRIVAL, arrival)
            put(TravelEntry.COLUMN_TIME, time)
            put(TravelEntry.COLUMN_TIMESTAMP, System.currentTimeMillis())
        }

        return db.insert(TravelEntry.TABLE_NAME, null, values)
    }

    fun getAllTravelRoutes(): Cursor {
        val db = readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            TravelEntry.COLUMN_DEPARTURE,
            TravelEntry.COLUMN_ARRIVAL,
            TravelEntry.COLUMN_TIME,
            TravelEntry.COLUMN_TIMESTAMP
        )

        return db.query(
            TravelEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            "${TravelEntry.COLUMN_TIMESTAMP} DESC"
        )
    }

    fun clearAllTravelRoutes(): Int {
        val db = writableDatabase
        return db.delete(TravelEntry.TABLE_NAME, null, null)
    }
}
