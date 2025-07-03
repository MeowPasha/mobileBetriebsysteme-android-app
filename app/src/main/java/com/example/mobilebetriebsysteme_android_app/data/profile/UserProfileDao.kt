package com.example.mobilebetriebsysteme_android_app.data.profile

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    //Gets the profile from the db.
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    //Adds or updates the profile.
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    //Deletes the profile.
    @Query("DELETE FROM user_profile")
    suspend fun deleteProfile()
}