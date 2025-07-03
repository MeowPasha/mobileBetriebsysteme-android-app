package com.example.mobilebetriebsysteme_android_app.bluetooth

import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the contract for Bluetooth operations within the app.
 */
interface BluetoothController {

    /**
     * A cold stream of devices discovered during an active scan.
     * Emits the current list of found BluetoothDevice objects.
     */
    val scannedDevices: StateFlow<List<BluetoothDevice>>

    /**
     * A cold stream of devices that are currently paired (bonded) with the local adapter.
     * Emits the current list of paired BluetoothDevice objects.
     */
    val pairedDevices: StateFlow<List<BluetoothDevice>>

    /**
     * Starts a BLE/classic Bluetooth discovery scan.
     * Discovered devices will be emitted through [scannedDevices].
     */
    fun startDiscovery()

    /**
     * Stops any ongoing Bluetooth discovery scan.
     * Subsequent calls to `scannedDevices` will not include new devices until a new scan is started.
     */
    fun stopDiscovery()

    /**
     * Attempts to establish a Bluetooth connection to the given [device].
     * This operation may involve socket creation, RFCOMM handshake, etc.
     *
     * @param device The target BluetoothDevice to connect to.
     */
    fun connectToServer(device: BluetoothDevice)

    /**
     * Releases any resources held by the controller, such as broadcast receivers.
     * Should be called when the controller is no longer needed to avoid leaks.
     */
    fun release()

    /**
     * Closes an active Bluetooth connection, if one exists.
     * Ensures sockets are closed and internal state reset.
     */
    fun closeConnection()

    fun isConnected(): Boolean
}