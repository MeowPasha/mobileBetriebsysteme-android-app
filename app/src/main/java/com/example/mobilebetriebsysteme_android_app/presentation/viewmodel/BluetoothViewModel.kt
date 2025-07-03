package com.example.mobilebetriebsysteme_android_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilebetriebsysteme_android_app.bluetooth.AndroidBluetoothController
import com.example.mobilebetriebsysteme_android_app.bluetooth.BluetoothController
import com.example.mobilebetriebsysteme_android_app.bluetooth.BluetoothDevice
import com.example.mobilebetriebsysteme_android_app.presentation.BluetoothUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _state
    ) { scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _connectionStatus = MutableStateFlow<String?>(null)
    val connectionStatus = _connectionStatus.asStateFlow()

    init {
        if (bluetoothController is AndroidBluetoothController) {
            bluetoothController.onConnectionResult = { success, deviceName ->
                _isConnected.value = success
                val msg = if (success) "Connection successful: $deviceName" else "Connection failed"
                _connectionStatus.value = msg
            }
        }
    }

    fun startScan() {
        _state.update { it.copy(isScanning = true) }
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        _state.update { it.copy(isScanning = false) }
        bluetoothController.stopDiscovery()
    }

    fun connectToDevice(device: BluetoothDevice) {
        if (bluetoothController is AndroidBluetoothController) {
            bluetoothController.connectToServer(device)
        } else {
            bluetoothController.connectToServer(device)
        }
    }

    fun startServer() {
        if (bluetoothController is AndroidBluetoothController) {
            bluetoothController.startServer()
        }
    }

    fun isBluetoothConnected(): Boolean = _isConnected.value

    fun stopAll() {
        bluetoothController.closeConnection()
        bluetoothController.stopDiscovery()
        _state.update { it.copy(isScanning = false) }
        _isConnected.value = false
    }
}