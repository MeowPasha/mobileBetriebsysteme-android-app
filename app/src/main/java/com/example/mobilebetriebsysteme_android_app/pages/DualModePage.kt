package com.example.mobilebetriebsysteme_android_app.pages

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

@Composable
fun DualModePage(
    viewModel: BluetoothViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    var showDialog by remember { mutableStateOf(true) }
    var dontShowAgain by remember { mutableStateOf(false) }

    val isConnecting by viewModel.isConnecting.collectAsState()


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dual Mode", style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons in 2x2 Grid
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = { viewModel.startScan() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start Scan")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.stopScan() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop Scan")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Button(onClick = { viewModel.startServer() }) {
                        Text("Start Server")
                    }

                    if (state.isScanning) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Scanning...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            connectionStatus?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (msg.startsWith("Connection successful")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Devices List
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Found Devices", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(state.scannedDevices) { device ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.connectToDevice(device) }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = device.name ?: "Unnamed")
                                    device.address?.let {
                                        Text(text = it, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text("Paired Devices", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(state.pairedDevices) { device ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.connectToDevice(device) }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = device.name ?: "Unnamed")
                                    Text(text = device.address, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog && !dontShowAgain) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("How to Connect in Dual Mode") },
                text = {
                    Column {
                        Text(
                            "To start a Dual Mode session, one device should tap 'Start Server'.\n" +
                                    "The other device must tap 'Start Scan' and select the wanted device from the list.\n" +
                                    "Once connected, you can go back and start a DualMode session from the map."
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = dontShowAgain,
                                onCheckedChange = { dontShowAgain = it }
                            )
                            Text("Don't show this again")
                        }
                    }
                }
            )
        }

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