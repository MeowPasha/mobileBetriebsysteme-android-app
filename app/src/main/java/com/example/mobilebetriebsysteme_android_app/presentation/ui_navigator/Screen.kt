package com.example.mobilebetriebsysteme_android_app.presentation.ui_navigator

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Profile : Screen("profile")
    object DualMode : Screen("dualmode")
}