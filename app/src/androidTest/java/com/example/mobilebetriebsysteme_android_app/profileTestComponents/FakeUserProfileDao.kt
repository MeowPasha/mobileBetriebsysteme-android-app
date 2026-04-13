package com.example.mobilebetriebsysteme_android_app.profileTestComponents

import com.example.mobilebetriebsysteme_android_app.data.profile.UserProfile
import com.example.mobilebetriebsysteme_android_app.data.profile.UserProfileDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserProfileDao : UserProfileDao {
    private val profileFlow = MutableStateFlow<UserProfile?>(null)

    override fun getProfile(): Flow<UserProfile?> = profileFlow

    override suspend fun insertProfile(profile: UserProfile) {
        profileFlow.value = profile
    }

    override suspend fun deleteProfile() {
        profileFlow.value = null
    }
}
