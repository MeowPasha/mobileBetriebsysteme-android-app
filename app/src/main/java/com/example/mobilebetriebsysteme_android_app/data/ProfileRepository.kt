package com.example.mobilebetriebsysteme_android_app.data

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(context: Context) {
    private val dbHelper = ProfileDbHelper(context)

    suspend fun saveProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ProfileContract.ProfileEntry.COLUMN_NAME_NAME, profile.name)
            put(ProfileContract.ProfileEntry.COLUMN_NAME_AGE, profile.age)
            put(ProfileContract.ProfileEntry.COLUMN_NAME_STEP_GOAL, profile.stepGoal)
        }
        // Eğer zaten bir satır varsa güncelle, yoksa ekle:
        val cursor = db.query(
            ProfileContract.ProfileEntry.TABLE_NAME,
            arrayOf(BaseColumns._ID),
            null, null, null, null, null, "1"
        )
        if (cursor.moveToFirst()) {
            val existingId = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
            db.update(
                ProfileContract.ProfileEntry.TABLE_NAME,
                values,
                "${BaseColumns._ID} = ?",
                arrayOf(existingId.toString())
            )
        } else {
            db.insert(ProfileContract.ProfileEntry.TABLE_NAME, null, values)
        }
        cursor.close()
        db.close()
    }

    suspend fun getProfile(): UserProfile? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            ProfileContract.ProfileEntry.TABLE_NAME,
            arrayOf(BaseColumns._ID,
                ProfileContract.ProfileEntry.COLUMN_NAME_NAME,
                ProfileContract.ProfileEntry.COLUMN_NAME_AGE,
                ProfileContract.ProfileEntry.COLUMN_NAME_STEP_GOAL),
            null, null, null, null, null, "1"
        )
        val profile = if (cursor.moveToFirst()) {
            UserProfile(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(ProfileContract.ProfileEntry.COLUMN_NAME_NAME)),
                age = cursor.getInt(cursor.getColumnIndexOrThrow(ProfileContract.ProfileEntry.COLUMN_NAME_AGE)),
                stepGoal = cursor.getInt(cursor.getColumnIndexOrThrow(ProfileContract.ProfileEntry.COLUMN_NAME_STEP_GOAL))
            )
        } else null
        cursor.close()
        db.close()
        profile
    }

    suspend fun deleteProfile() = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(ProfileContract.ProfileEntry.TABLE_NAME, null, null)
        db.close()
    }
}
