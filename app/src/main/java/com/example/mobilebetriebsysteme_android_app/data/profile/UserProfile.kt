package com.example.mobilebetriebsysteme_android_app.data.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Long = 0,
    val name: String,
    val age: Int,
    val height: Int,
    val stepGoal: Int
)