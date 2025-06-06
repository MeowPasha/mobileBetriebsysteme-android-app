package com.example.mobilebetriebsysteme_android_app.pages

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.mobilebetriebsysteme_android_app.presantation.viewmodel.BluetoothViewModel

@Composable
fun DualModePage(viewModel: BluetoothViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Dual Mode Page", style = MaterialTheme.typography.headlineMedium)
    }
}