package com.example.mobilebetriebsysteme_android_app.presantation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.mobilebetriebsysteme_android_app.presantation.ui_navigator.MyAppNavigator

class MainActivity : ComponentActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                MaterialTheme {
                    MyAppNavigator()
                }
            }
        }
}