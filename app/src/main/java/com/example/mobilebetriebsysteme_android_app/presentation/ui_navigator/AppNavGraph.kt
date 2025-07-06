package com.example.mobilebetriebsysteme_android_app.presentation.ui_navigator

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobilebetriebsysteme_android_app.pages.DualModePage
import com.example.mobilebetriebsysteme_android_app.pages.MainPage
import com.example.mobilebetriebsysteme_android_app.pages.ProfilePage
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.BluetoothViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.profileVM.ProfileViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.walkingSessionVM.WalkingSessionViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    bluetoothViewModel: BluetoothViewModel
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val walkingSessionViewModel: WalkingSessionViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainPage(
                navController = navController,
                walkingSessionVM = walkingSessionViewModel,
                bluetoothViewModel = bluetoothViewModel
            )
        }
        composable(Screen.Profile.route) {
            ProfilePage(
                viewModel = profileViewModel,
                walkingSessionViewModel = walkingSessionViewModel,
                onClose = { navController.popBackStack() }
            )
        }
        composable(Screen.DualMode.route) {
            DualModePage(
                viewModel = bluetoothViewModel,
                onClose = { navController.popBackStack() }
            )
        }
    }
}


