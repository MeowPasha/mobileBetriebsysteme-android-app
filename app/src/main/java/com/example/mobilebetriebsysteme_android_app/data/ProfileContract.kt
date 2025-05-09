package com.example.mobilebetriebsysteme_android_app.data

import android.provider.BaseColumns

// A contract class is a container for constants that define names for URIs, tables, and columns.
object ProfileContract {
    object ProfileEntry : BaseColumns {
        const val TABLE_NAME = "user_profile"
        const val COLUMN_NAME_NAME = "name"
        const val COLUMN_NAME_AGE = "age"
        const val COLUMN_NAME_STEP_GOAL = "step_goal"
    }
}