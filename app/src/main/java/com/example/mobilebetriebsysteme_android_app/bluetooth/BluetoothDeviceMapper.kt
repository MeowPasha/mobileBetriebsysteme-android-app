package com.example.mobilebetriebsysteme_android_app.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

//typealias BluetoothDeviceDomain = BluetoothDevice

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}

