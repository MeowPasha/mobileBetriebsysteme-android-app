package com.example.mobilebetriebsysteme_android_app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilebetriebsysteme_android_app.pages.DualModePage
import com.example.mobilebetriebsysteme_android_app.pages.HomePage
import com.example.mobilebetriebsysteme_android_app.pages.ProfilePage
import com.example.mobilebetriebsysteme_android_app.viewmodel.ProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilebetriebsysteme_android_app.pages.MainPage

@Composable
fun MyAppNavigator() {
    val profileViewModel: ProfileViewModel = viewModel()
    val navController = rememberNavController()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { MainPage(
                context = context
            ) }
            composable("dualmode") { DualModePage() }
            composable("profile") { ProfilePage(
                viewModel = profileViewModel
            ) }

        }
    }
}
