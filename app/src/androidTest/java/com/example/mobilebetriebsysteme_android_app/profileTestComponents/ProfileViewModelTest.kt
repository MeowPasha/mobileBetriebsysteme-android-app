package com.example.mobilebetriebsysteme_android_app.profileTestComponents

import com.example.mobilebetriebsysteme_android_app.data.profile.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    // Test coroutine dispatcher
    @get:Rule

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeUserProfileDao
    private lateinit var viewModel: ProfileViewModelForTest

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeUserProfileDao()
        viewModel = ProfileViewModelForTest(fakeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveProfileSetsProfileCorrectly() = runTest {
        val profile = UserProfile(name = "Alice", age = 25, height = 165, stepGoal = 10000)
        viewModel.saveProfile(profile)
        advanceUntilIdle()

        val result = viewModel.profile.value
        Assert.assertEquals(profile, result)
    }

    @Test
    fun deleteProfileClearsProfile() = runTest {
        val profile = UserProfile(name = "Bob", age = 30, height = 180, stepGoal = 8000)
        viewModel.saveProfile(profile)
        advanceUntilIdle()

        viewModel.deleteProfile()
        advanceUntilIdle()

        val result = viewModel.profile.value
        Assert.assertNull(result)
    }
}