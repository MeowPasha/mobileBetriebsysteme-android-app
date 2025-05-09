package com.example.mobilebetriebsysteme_android_app.data

// This class defines the each Datatype for the each column.

data class UserProfile(
    val id: Long = 0,
    val name: String,
    val age: Int,
    val stepGoal: Int
)