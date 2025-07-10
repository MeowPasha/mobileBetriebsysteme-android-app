package com.example.mobilebetriebsysteme_android_app.presentation.viewmodel

import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilebetriebsysteme_android_app.bluetooth.AndroidBluetoothController
import com.example.mobilebetriebsysteme_android_app.bluetooth.BluetoothController
import com.example.mobilebetriebsysteme_android_app.bluetooth.BluetoothDevice
import com.example.mobilebetriebsysteme_android_app.presentation.BluetoothUiState
import com.example.mobilebetriebsysteme_android_app.data.session.WalkingSessionEntity
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.walkingSessionVM.WalkingSessionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    internal val bluetoothController: BluetoothController
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

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    private val _showChallengeDialog = MutableStateFlow(false)
    val showChallengeDialog: StateFlow<Boolean> = _showChallengeDialog

    private val _challengeDuration = MutableStateFlow(0)
    val challengeDuration: StateFlow<Int> = _challengeDuration

    private val _challengeAccepted = MutableStateFlow(false)
    val challengeAccepted = _challengeAccepted.asStateFlow()

    private val _challengeFromUser = MutableStateFlow<String>("Unknown")
    val challengeFromUser = _challengeFromUser.asStateFlow()

    private val _sessionEndedByRemote = MutableStateFlow(false)
    val sessionEndedByRemote = _sessionEndedByRemote.asStateFlow()

    private val _macAddress = MutableStateFlow<String?>(null)
    val macAddress: StateFlow<String?> = _macAddress.asStateFlow()

    private val _isCompareRequest = MutableStateFlow(false)
    val isCompareRequest = _isCompareRequest.asStateFlow()


    private var currentMACAddress: String? = null

    // Injected from outside
    private lateinit var walkingSessionViewModel: WalkingSessionViewModel

    fun setWalkingSessionViewModel(viewModel: WalkingSessionViewModel) {
        walkingSessionViewModel = viewModel
    }

    init {
        if (bluetoothController is AndroidBluetoothController) {
            bluetoothController.onConnectionResult = { success, deviceName ->
                _isConnecting.value = false
                _isConnected.value = success
                _connectionStatus.value = if (success) {
                    "Connection successful: $deviceName"
                } else {
                    "Connection failed"
                }

                if (success) {
                    Log.d("BluetoothViewModel", "Connection successful")
                    val socket = bluetoothController.socket
                    currentMACAddress = socket?.remoteDevice?.address
                    _macAddress.value = currentMACAddress
                    Log.d("BluetoothViewModel", "MAC Address: $currentMACAddress")
                }
                setupBluetoothCallbacks(bluetoothController)
            }
            setupBluetoothCallbacks(bluetoothController)
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
            _isConnecting.value = true
            bluetoothController.connectToServer(device)
        }
    }

    fun startServer() {
        if (bluetoothController is AndroidBluetoothController) {
            bluetoothController.startServer()
        }
    }

    fun sendMessage(message: String) {
        Log.d("BluetoothViewModel", "Sending message: $message")
        if (bluetoothController is AndroidBluetoothController) {
            bluetoothController.sendMessage(message)
        }
    }

    fun acceptChallenge() {
        _showChallengeDialog.value = false
        _challengeAccepted.value = true

        val response = JSONObject()
        response.put("type", "challenge_response")
        response.put("accepted", true)
        response.put("duration", _challengeDuration.value)

        sendMessage(response.toString())
    }

    fun declineChallenge() {
        _showChallengeDialog.value = false
        _challengeAccepted.value = false

        val response = JSONObject()
        response.put("type", "challenge_response")
        response.put("accepted", false)

        sendMessage(response.toString())
    }

    fun sendSessionEnd() {
        val json = JSONObject().apply {
            put("type", "session_end")
        }
        sendMessage(json.toString())
    }

    fun sendCompareRequest() {
        val message = JSONObject().apply {
            put("type", "compare_request")
            put("text", "Do you want to compare walking sessions?")
        }
        sendMessage(message.toString())
    }

    fun resetChallengeAcceptedFlag() {
        _challengeAccepted.value = false
    }

    fun resetSessionEndedFlag() {
        _sessionEndedByRemote.value = false
    }

    fun getMacAddress(): String? {
        Log.d("BluetoothViewModel, returning MAC Address", "MAC Address: $currentMACAddress")
        return currentMACAddress
    }

    fun setupBluetoothCallbacks(bluetoothController: AndroidBluetoothController) {
        bluetoothController.onMessageReceived = { message ->
            Log.d("BluetoothViewModel", "Received message: $message")

            val jsonStrings = message.split("}{").mapIndexed { index, part ->
                when (index) {
                    0 -> part + "}"
                    message.split("}{").lastIndex -> "{" + part
                    else -> "{" + part + "}"
                }
            }

            jsonStrings.forEach { jsonStr ->
                try {
                    val json = JSONObject(jsonStr)
                    when (json.getString("type")) {
                        "challenge" -> {
                            val duration = json.getInt("duration")
                            val fromUser = json.optString("from", "Unknown")
                            _challengeDuration.value = duration
                            _challengeFromUser.value = fromUser
                            _showChallengeDialog.value = true
                            _isCompareRequest.value = false
                            Log.d("BluetoothViewModel", "Challenge received, show dialog: true, duration=$duration")
                        }

                        "challenge_response" -> {
                            val accepted = json.getBoolean("accepted")
                            val duration = json.optInt("duration", 0)
                            if (accepted) {
                                _challengeAccepted.value = true
                                _connectionStatus.value = "Challenge accepted! Duration: $duration min"
                            } else {
                                _challengeAccepted.value = false
                                _connectionStatus.value = "Challenge declined by user."
                            }
                        }
                        "session_end" -> {
                            Log.d("BluetoothViewModel", "Session end received from remote")
                            _sessionEndedByRemote.value = true
                        }
                        "compare_request" -> {
                            val text = json.optString("text", "Do you want to compare walking sessions?")
                            _connectionStatus.value = text
                            _isCompareRequest.value = true
                            _showChallengeDialog.value = true
                            Log.d("BluetoothViewModel", "Comparison request received: $text")
                        }
                        else -> {
                            Log.d("BluetoothViewModel", "Unknown message type: ${json.getString("type")}")
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("BluetoothViewModel", "Failed to parse JSON: $jsonStr")
                }
            }
        }
    }

    fun stopAll() {
        bluetoothController.closeConnection()
        bluetoothController.stopDiscovery()
        _state.update { it.copy(isScanning = false) }
        _isConnected.value = false
    }
}