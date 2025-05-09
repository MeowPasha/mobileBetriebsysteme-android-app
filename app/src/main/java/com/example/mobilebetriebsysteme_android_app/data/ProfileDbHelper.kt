package com.example.mobilebetriebsysteme_android_app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

//The Helper class contains methods that create and maintain the database and tables.

private const val DATABASE_NAME = "Profile.db"
private const val DATABASE_VERSION = 1

class ProfileDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_ENTRIES = """
            CREATE TABLE ${ProfileContract.ProfileEntry.TABLE_NAME} (
              ${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT,
              ${ProfileContract.ProfileEntry.COLUMN_NAME_NAME} TEXT NOT NULL,
              ${ProfileContract.ProfileEntry.COLUMN_NAME_AGE} INTEGER NOT NULL,
              ${ProfileContract.ProfileEntry.COLUMN_NAME_STEP_GOAL} INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Basit tutuyoruz: versiyon değişirse tabloyu sil ve yeniden oluştur
        db.execSQL("DROP TABLE IF EXISTS ${ProfileContract.ProfileEntry.TABLE_NAME}")
        onCreate(db)
    }
}