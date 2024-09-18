package com.example.cu

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "VisitorManagement.db"
        const val DATABASE_VERSION = 1

        // Table Names
        const val TABLE_VISITORS = "visitors"
        // Define other tables as needed

        // Visitor Table Columns
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PURPOSE = "purpose"
        const val COLUMN_STATUS = "status"
        const val COLUMN_ARRIVAL_DATE_TIME = "arrival_date_time"
        // Add other columns
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create tables
        db?.execSQL("CREATE TABLE $TABLE_VISITORS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_PURPOSE TEXT," +
                "$COLUMN_STATUS TEXT," +
                "$COLUMN_ARRIVAL_DATE_TIME TEXT" +
                // Add other columns
                ")")
        // Create other tables as needed
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Upgrade database schema if needed
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_VISITORS")
        onCreate(db)
        // Add upgrade logic for other tables
    }
}
