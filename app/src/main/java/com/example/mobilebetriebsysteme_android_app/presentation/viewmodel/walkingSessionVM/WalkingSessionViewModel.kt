package com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.walkingSessionVM

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilebetriebsysteme_android_app.data.session.SessionDatabase
import com.example.mobilebetriebsysteme_android_app.data.session.WalkingSessionEntity
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

class WalkingSessionViewModel(application: Application) : AndroidViewModel(application) {

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

    val steps: StateFlow<Int> = distanceInMeters
        .map { distance -> (distance / averageStepLength).toInt() }
        .stateIn(viewModelScope, SharingStarted.Companion.Eagerly, 0)

    fun startSession(dualMode: Boolean = false) {
        _isDualModeSession.value = dualMode
        _isSessionActive.value = true
        _sessionDurationInSeconds.value = 0
        _distanceInMeters.value = 0f
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
                        isDualMode = _isDualModeSession.value
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
                _sessionDurationInSeconds.value += 1
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

    fun resetSessionData() {
        _sessionDurationInSeconds.value = 0
        _distanceInMeters.value = 0f
        _isDualModeSession.value = false
        _destinationPoint.value = null
    }

    fun setDestination(geoPoint: GeoPoint) {
        _destinationPoint.value = geoPoint
    }

    fun clearDestination() {
        _destinationPoint.value = null
    }
}