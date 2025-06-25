package com.example.mobilebetriebsysteme_android_app.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.BluetoothViewModel

@Composable
fun DualModePage(
    viewModel: BluetoothViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Dual Mode Page", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = { viewModel.startScan() }) {
                Text("Start Scan")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { viewModel.stopScan() }) {
                Text("Stop Scan")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Found Devices:", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(state.scannedDevices) { device ->
                Text("- ${device.name ?: "Unnamed"} | ${device.address}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Paired Devices:", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(state.pairedDevices) { device ->
                Text("- ${device.name ?: "Unnamed"} | ${device.address}")
            }
        }
    }
}
