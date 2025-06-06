package com.example.mobilebetriebsysteme_android_app.bluetoothPackage

typealias BluetoothDeviceDomain = BluetoothDevice

fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}

