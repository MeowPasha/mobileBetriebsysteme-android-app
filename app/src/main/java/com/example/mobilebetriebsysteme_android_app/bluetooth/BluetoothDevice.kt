package com.example.mobilebetriebsysteme_android_app.bluetooth

typealias  BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name: String?,
    val address: String
)
