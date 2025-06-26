package com.example.mobilebetriebsysteme_android_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// This class defines the Entity that will be restored in db.

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Long = 0,
    val name: String,
    val age: Int,
    val height: Int,
    val stepGoal: Int
)