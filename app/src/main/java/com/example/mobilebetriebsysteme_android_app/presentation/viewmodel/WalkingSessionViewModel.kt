package com.example.mobilebetriebsysteme_android_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WalkingSessionViewModel : ViewModel() {

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive = _isSessionActive.asStateFlow()

    private val _sessionDurationInSeconds = MutableStateFlow(0)
    val sessionDurationInSeconds = _sessionDurationInSeconds.asStateFlow()

    private val _distanceInMeters = MutableStateFlow(0f)
    val distanceInMeters = _distanceInMeters.asStateFlow()

    fun startSession() {
        _isSessionActive.value = true
        _sessionDurationInSeconds.value = 0
        _distanceInMeters.value = 0f
        startTimer()
    }

    fun stopSession() {
        _isSessionActive.value = false
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_isSessionActive.value) {
                delay(1000)
                _sessionDurationInSeconds.value += 1
            }
        }
    }

    fun updateDistance(newDistance: Float) {
        _distanceInMeters.value += newDistance
    }
}
