package com.example.mobilebetriebsysteme_android_app.profileTestComponents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilebetriebsysteme_android_app.data.profile.UserProfile
import com.example.mobilebetriebsysteme_android_app.data.profile.UserProfileDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModelForTest(private val dao: UserProfileDao) : AndroidViewModel(Application()) {
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
