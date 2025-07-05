package com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.walkingSessionVM

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WalkingSessionViewModelFactory(
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalkingSessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WalkingSessionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}