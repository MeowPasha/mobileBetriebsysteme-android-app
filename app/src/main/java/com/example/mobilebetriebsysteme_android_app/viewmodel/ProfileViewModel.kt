package com.example.mobilebetriebsysteme_android_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilebetriebsysteme_android_app.data.ProfileRepository
import com.example.mobilebetriebsysteme_android_app.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository(application)

    private val _profileState = MutableStateFlow<UserProfile?>(null)
    val profileState: StateFlow<UserProfile?> = _profileState

    init {
        viewModelScope.launch {
            _profileState.value = repository.getProfile()
        }
    }

    fun saveProfile(name: String, age: Int, stepGoal: Int) {
        viewModelScope.launch {
            repository.saveProfile(UserProfile(name = name, age = age, stepGoal = stepGoal))
            _profileState.value = repository.getProfile()
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            repository.deleteProfile()
            _profileState.value = null
        }
    }
}