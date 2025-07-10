package com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.walkingSessionVM

import android.R
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilebetriebsysteme_android_app.bluetooth.BluetoothController
import com.example.mobilebetriebsysteme_android_app.data.session.SessionDatabase
import com.example.mobilebetriebsysteme_android_app.data.session.WalkingSessionEntity
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.BluetoothViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.util.UUID

class WalkingSessionViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val sessionDao = SessionDatabase.Companion.getDatabase(application).sessionDao()
    private val averageStepLength = 0.75f

    val allSessions: Flow<List<WalkingSessionEntity>> = sessionDao.getAllSessions()

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive = _isSessionActive.asStateFlow()

    private val _sessionDurationInSeconds = MutableStateFlow(0)
    val sessionDurationInSeconds = _sessionDurationInSeconds.asStateFlow()

    private val _distanceInMeters = MutableStateFlow(0f)
    val distanceInMeters = _distanceInMeters.asStateFlow()

    private val _destinationPoint = MutableStateFlow<GeoPoint?>(null)
    val destinationPoint: StateFlow<GeoPoint?> = _destinationPoint

    private val _isDualModeSession = MutableStateFlow(false)
    val isDualModeSession = _isDualModeSession.asStateFlow()

    private val _sessionSavedEvent = MutableSharedFlow<Boolean>()
    val sessionSavedEvent = _sessionSavedEvent.asSharedFlow()

    private val _sessionKey = MutableStateFlow("")
    val sessionKey = _sessionKey.asStateFlow()

    private val _opponentMAC = MutableStateFlow("")
    val opponentMAC = _opponentMAC.asStateFlow()

    private val _connectedTo = MutableStateFlow("")
    val connectedTo = _connectedTo.asStateFlow()

    private val _isCompared = MutableStateFlow(false)
    val isCompared: StateFlow<Boolean> = _isCompared.asStateFlow()

    val steps: StateFlow<Int> = distanceInMeters
        .map { distance -> (distance / averageStepLength).toInt() }
        .stateIn(viewModelScope, SharingStarted.Companion.Eagerly, 0)

    private var startTimeMillis: Long? = null

    fun startSession(
        dualMode: Boolean = false,
        startTimeMillis: Long? = null,
    ) {
        _isDualModeSession.value = dualMode
        _isSessionActive.value = true
        _distanceInMeters.value = 0f
        this.startTimeMillis = startTimeMillis ?: System.currentTimeMillis()

        if (_isDualModeSession.value && _sessionKey.value.isBlank()) {
            sessionKeyCreator()
        }
        startTimer()
    }

    fun stopSessionAndSave() {
        if (_isSessionActive.value) {
            _isSessionActive.value = false
            viewModelScope.launch {
                if (_sessionDurationInSeconds.value >= 60) {
                    val session = WalkingSessionEntity(
                        durationSeconds = _sessionDurationInSeconds.value,
                        distanceMeters = _distanceInMeters.value,
                        isDualMode = _isDualModeSession.value,
                        sessionKey = if (_isDualModeSession.value) _sessionKey.value else "",
                        opponentMAC = if (_isDualModeSession.value) _opponentMAC.value else "",
                        isCompared = false
                    )
                    sessionDao.insertSession(session)
                    _sessionSavedEvent.emit(true)
                } else {
                    _sessionSavedEvent.emit(false)
                }
                resetSessionData()
            }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_isSessionActive.value) {
                delay(1000)
                val now = System.currentTimeMillis()
                startTimeMillis?.let {
                    val duration = ((now - it) / 1000).toInt()
                    _sessionDurationInSeconds.value = duration
                }
            }
        }
    }

    fun updateDistance(newDistanceInMeters: Float) {
        if (!_isSessionActive.value) return

        if (newDistanceInMeters >= 5f) {
            _distanceInMeters.value += newDistanceInMeters
        }
    }

    fun deleteSession(session: WalkingSessionEntity) {
        viewModelScope.launch {
            sessionDao.deleteSession(session)
        }
    }

    fun calculateSteps(distanceMeters: Float): Int {
        return (distanceMeters / averageStepLength).toInt()
    }

    fun setOpponentMAC(macaddress: String) {
        Log.d("Setting opponent MAC Address", "MAC Address: $macaddress")
        _opponentMAC.value = macaddress
    }

    fun sessionKeyCreator() {
        if (_sessionKey.value.isBlank()) {
            _sessionKey.value = "DMS-" + UUID.randomUUID().toString()
        }
    }

    fun connectedTo(macaddress: String) {
        _connectedTo.value = macaddress
    }

    fun askOpponentToCompare(bluetoothViewModel: BluetoothViewModel) {
        viewModelScope.launch {
            bluetoothViewModel.sendCompareRequest()
            Log.d("Compare", "Comparison request sent to opponent.")
        }

    }


    fun compareSessions(session: WalkingSessionEntity,bluetoothViewModel: BluetoothViewModel) {
        viewModelScope.launch {
            if (session.isDualMode && session.sessionKey.isNotBlank() && session.opponentMAC.isNotBlank() && !session.isCompared) {
                var currentConnectedMac = _connectedTo.value
                val wasConnectedTo = session.opponentMAC.toString()

                askOpponentToCompare(bluetoothViewModel)

                Log.d("Compare", "Connected device MAC: $currentConnectedMac")
                Log.d("Compare", "Session's opponent MAC: $wasConnectedTo")

                // MAC adresleri eşleşiyor mu?
                if (currentConnectedMac == wasConnectedTo) {
                    Log.d("Compare", "MAC addresses match, proceeding with comparison")


                } else {
                    Log.d("Compare", "MAC address does NOT match. Comparison aborted.")
                }

                Log.d(
                    "Mac Address is comparedSessions",
                    "Comparing session with key: $_opponentMAC"
                )

            } else {
                Log.d(
                    "Mac Address is comparedSessions",
                    "Session is not dual mode or session key is blank or opponent MAC is blank"
                )
            }
        }
    }

    fun resetSessionData() {
        _sessionDurationInSeconds.value = 0
        _distanceInMeters.value = 0f
        _isDualModeSession.value = false
        _destinationPoint.value = null
        _sessionKey.value = ""
        _opponentMAC.value = ""
    }

    fun setDestination(geoPoint: GeoPoint) {
        _destinationPoint.value = geoPoint
    }

    fun clearDestination() {
        _destinationPoint.value = null
    }
}