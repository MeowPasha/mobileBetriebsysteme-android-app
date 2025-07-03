package com.example.mobilebetriebsysteme_android_app.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class AndroidBluetoothController @Inject constructor(
    private val context: Context
) : BluetoothController {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val foundDeviceReciever = FoundDeviceReciever { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null

    var onConnectionResult: ((success: Boolean, deviceName: String?) -> Unit)? = null

    init {
        updatePairedDevices()
    }

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        context.registerReceiver(
            foundDeviceReciever,
            IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND)
        )
        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    override fun stopDiscovery() {
        if (!hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun startServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val adapter = bluetoothAdapter ?: return@launch
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                serverSocket = adapter.listenUsingRfcommWithServiceRecord("MyAppServer", uuid)

                while (true) {
                    val socket = serverSocket?.accept() ?: break
                    clientSocket = socket
                    println("Server connected to: ${socket.remoteDevice.name}")
                    onConnectionResult?.invoke(true, socket.remoteDevice.name)
                    // Daha sonra veri okuma/yazma eklenebilir
                }
            } catch (e: IOException) {
                e.printStackTrace()
                onConnectionResult?.invoke(false, null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun connectToServer(device: BluetoothDeviceDomain) {
        bluetoothAdapter?.cancelDiscovery()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                val realDevice = bluetoothAdapter?.getRemoteDevice(device.address)
                val socket = realDevice?.createRfcommSocketToServiceRecord(uuid)
                socket?.connect()
                clientSocket = socket
                println("Client connected to: ${device.name}")
                onConnectionResult?.invoke(true, device.name)
            } catch (e: IOException) {
                e.printStackTrace()
                println("Client connection failed: ${e.message}")
                onConnectionResult?.invoke(false, null)
            }
        }
    }

    override fun closeConnection() {
        try {
            clientSocket?.close()
            clientSocket = null
            serverSocket?.close()
            serverSocket = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun isConnected(): Boolean {
        return clientSocket?.isConnected ?: false
    }


    override fun release() {
        context.unregisterReceiver(foundDeviceReciever)
    }

    @SuppressLint("MissingPermission")
    private fun updatePairedDevices() {
        if (!hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}