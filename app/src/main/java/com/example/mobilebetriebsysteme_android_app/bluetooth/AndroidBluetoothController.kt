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

    internal var socket: BluetoothSocket? = null
    var onMessageReceived: ((String) -> Unit)? = null

    var onConnectionResult: ((success: Boolean, deviceName: String?) -> Unit)? = null
    private var isReceiverRegistered = false

    init {
        updatePairedDevices()
    }

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return

        if (!isReceiverRegistered) {
            context.registerReceiver(
                foundDeviceReciever,
                IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND)
            )
            isReceiverRegistered = true
        }
        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    @SuppressLint("MissingPermission")
    override fun startServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val adapter = bluetoothAdapter ?: return@launch
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                serverSocket = adapter.listenUsingRfcommWithServiceRecord("MyAppServer", uuid)

                while (true) {
                    val acceptedSocket = serverSocket?.accept() ?: break
                    clientSocket = acceptedSocket
                    socket = acceptedSocket  // Burada socket ataması önemli
                    println("Server connected to: ${acceptedSocket.remoteDevice.name}")
                    onConnectionResult?.invoke(true, acceptedSocket.remoteDevice.name)

                    startListening(acceptedSocket)
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
                val newSocket = realDevice?.createRfcommSocketToServiceRecord(uuid)
                newSocket?.connect()
                clientSocket = newSocket
                socket = newSocket
                println("Client connected to: ${device.name}")
                onConnectionResult?.invoke(true, device.name)

                startListening(newSocket)
            } catch (e: IOException) {
                e.printStackTrace()
                println("Client connection failed: ${e.message}")
                onConnectionResult?.invoke(false, null)
            }
        }
    }

    override fun sendMessage(message: String) {
        try {
            socket?.outputStream?.apply {
                write(message.toByteArray(Charsets.UTF_8))
                flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun startListening(socket: BluetoothSocket?) {
        CoroutineScope(Dispatchers.IO).launch {
            val inputStream = socket?.inputStream
            val buffer = ByteArray(1024)
            try {
                while (true) {
                    val bytesRead = inputStream?.read(buffer)
                    if (bytesRead != null && bytesRead > 0) {
                        val message = String(buffer, 0, bytesRead, Charsets.UTF_8)
                        println("Received message: $message")
                        onMessageReceived?.invoke(message)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun closeConnection() {
        try {
            clientSocket?.close()
            clientSocket = null
            serverSocket?.close()
            serverSocket = null
            socket = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun isConnected(): Boolean {
        return clientSocket?.isConnected ?: false
    }

    override fun release() {
        if (isReceiverRegistered) {
            context.unregisterReceiver(foundDeviceReciever)
            isReceiverRegistered = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
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
