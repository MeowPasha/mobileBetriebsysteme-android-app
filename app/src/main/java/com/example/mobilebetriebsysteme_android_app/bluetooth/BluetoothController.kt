package com.example.mobilebetriebsysteme_android_app.bluetooth

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface that defines the core Bluetooth functionalities required by the app.
 * This abstraction supports both client and server Bluetooth roles and allows reactive state updates.
 */
interface BluetoothController {

    /**
     * A StateFlow emitting a list of Bluetooth devices discovered during scanning.
     * This list updates in real-time as devices are found.
     * Useful for displaying available devices to connect to.
     */
    val scannedDevices: StateFlow<List<BluetoothDevice>>

    /**
     * A StateFlow emitting a list of Bluetooth devices that are already paired (bonded) with the device.
     * These are typically persistent and do not require new scanning.
     */
    val pairedDevices: StateFlow<List<BluetoothDevice>>

    /**
     * Starts a discovery process to find nearby Bluetooth devices.
     * Discovered devices are pushed into [scannedDevices].
     * Should be stopped using [stopDiscovery] to conserve resources.
     */
    fun startDiscovery()

    /**
     * Stops the ongoing Bluetooth discovery process.
     * After calling this, [scannedDevices] will no longer be updated with new devices.
     */
    fun stopDiscovery()

    /**
     * Initiates a connection to the specified Bluetooth device in client mode.
     *
     * @param device The target device to connect to. Must be a valid, discoverable or paired device.
     * Once connected, messages can be sent via [sendMessage].
     */
    fun connectToServer(device: BluetoothDevice)

    /**
     * Releases all internally held system resources, such as BroadcastReceivers or listeners.
     * This should be called when the controller is no longer used, typically in onDestroy.
     */
    fun release()

    /**
     * Closes an active Bluetooth connection, if any.
     * Ensures sockets are closed and internal references are cleared to avoid memory leaks.
     */
    fun closeConnection()

    /**
     * Starts a Bluetooth server that listens for incoming connection requests.
     * Typically used when the app is acting as the host in a Dual Mode session.
     */
    fun startServer()

    /**
     * Sends a text-based message over the currently active Bluetooth connection.
     * The message format is typically JSON to enable structured communication.
     *
     * @param message The string message to be transmitted.
     */
    fun sendMessage(message: String)

    /**
     * Returns whether a Bluetooth connection is currently established.
     *
     * @return true if connected, false otherwise.
     */
    fun isConnected(): Boolean
}
