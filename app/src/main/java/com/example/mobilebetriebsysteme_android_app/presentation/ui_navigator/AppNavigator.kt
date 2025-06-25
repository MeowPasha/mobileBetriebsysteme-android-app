package com.example.mobilebetriebsysteme_android_app.presentation.ui_navigator

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilebetriebsysteme_android_app.pages.DualModePage
import com.example.mobilebetriebsysteme_android_app.pages.ProfilePage
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.ProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilebetriebsysteme_android_app.pages.MainPage
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.BluetoothViewModel

@Composable
fun MyAppNavigator() {
    val profileViewModel: ProfileViewModel = viewModel()
    val bluetoothViewModel: BluetoothViewModel = hiltViewModel<BluetoothViewModel>()
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
            composable("home") {
                MainPage()
            }
        }
    }
}
