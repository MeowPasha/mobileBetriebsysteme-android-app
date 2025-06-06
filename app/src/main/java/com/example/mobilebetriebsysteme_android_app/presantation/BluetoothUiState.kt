package com.example.mobilebetriebsysteme_android_app.presantation
import com.example.mobilebetriebsysteme_android_app.bluetoothPackage.BluetoothDevice

data class BluetoothUiState(

    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
)





