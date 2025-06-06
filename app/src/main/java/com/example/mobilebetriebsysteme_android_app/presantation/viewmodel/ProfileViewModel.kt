package com.example.mobilebetriebsysteme_android_app.presantation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilebetriebsysteme_android_app.data.ProfileDatabase
import com.example.mobilebetriebsysteme_android_app.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ProfileDatabase.Companion.getDatabase(application).userProfileDao()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    init {
        viewModelScope.launch {
            dao.getProfile().collect {
                _profile.value = it
            }
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            dao.insertProfile(profile)
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            dao.deleteProfile()
        }
    }
}