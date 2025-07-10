package com.example.mobilebetriebsysteme_android_app.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.BluetoothViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.walkingSessionVM.WalkingSessionViewModel

@Composable
fun DualModePage(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
    walkingSessionViewModel: WalkingSessionViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    // Collect Bluetooth state
    val state by bluetoothViewModel.state.collectAsState()
    val connectionStatus by bluetoothViewModel.connectionStatus.collectAsState()
    val isConnecting by bluetoothViewModel.isConnecting.collectAsState()

    // Collect walking session stats
    val myDistance by walkingSessionViewModel.distanceInMeters.collectAsState()
    val myDuration by walkingSessionViewModel.sessionDurationInSeconds.collectAsState()

    var showInfoDialog by remember { mutableStateOf(true) }
    var dontShowAgain by remember { mutableStateOf(false) }

    val macAddress by bluetoothViewModel.macAddress.collectAsState()

    // Link BluetoothViewModel to WalkingSessionViewModel
    LaunchedEffect(Unit) {
        bluetoothViewModel.setWalkingSessionViewModel(walkingSessionViewModel)
    }

    LaunchedEffect(macAddress) {
        macAddress?.let {
            walkingSessionViewModel.setOpponentMAC(bluetoothViewModel.getMacAddress().toString())
            walkingSessionViewModel.connectedTo(bluetoothViewModel.getMacAddress().toString())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Title centered with close button at top right
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚔️ Step Duel",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Buttons: Start Scan, Stop Scan, Start Server
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { bluetoothViewModel.startScan() },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Start Scan", style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = { bluetoothViewModel.stopScan() },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Stop Scan", style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = { bluetoothViewModel.startServer() },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text("Start Server", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Show connection status message
            connectionStatus?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (msg.startsWith("Connection successful")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lists of devices: found and paired
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    Text("Found Devices", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(state.scannedDevices) { device ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { bluetoothViewModel.connectToDevice(device) }
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(text = device.name ?: "Unnamed", style = MaterialTheme.typography.bodyMedium)
//                                    device.address?.let {
//                                        Text(text = it, style = MaterialTheme.typography.bodySmall)
//                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                ) {
                    Text("Paired Devices", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(state.pairedDevices) { device ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { bluetoothViewModel.connectToDevice(device) }
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(text = device.name ?: "Unnamed", style = MaterialTheme.typography.bodyMedium)
//                                    Text(text = device.address, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Information dialog shown on first launch
        if (showInfoDialog && !dontShowAgain) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("How to Connect in Step Duel") },
                text = {
                    Column {
                        Text(
                            "To start a Step Duel, one device should tap 'Start Server'.\n" +
                                    "The other device must tap 'Start Scan' and select the device from the list.\n" +
                                    "Once connected, you can go back and begin your duel from the map."
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = dontShowAgain, onCheckedChange = { dontShowAgain = it })
                            Text("Don't show this again")
                        }
                    }
                }
            )
        }

        // Overlay shown during connection process
        if (isConnecting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Connecting... Please wait", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

// Extension function to format float to string with fixed decimals
fun Float.format(digits: Int) = "%.${digits}f".format(this)
