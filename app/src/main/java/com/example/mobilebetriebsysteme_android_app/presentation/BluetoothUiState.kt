package com.example.mobilebetriebsysteme_android_app.presentation
import com.example.mobilebetriebsysteme_android_app.bluetooth.BluetoothDevice

data class BluetoothUiState(

    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
)





