package com.example.mobilebetriebsysteme_android_app.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.mobilebetriebsysteme_android_app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch


@Composable
fun ProfilePage(viewModel: ProfileViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Get the profile data if there is one (Flow -> State)
    val userProfile by viewModel.profile.collectAsState()

    // Typing box
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var age by remember { mutableStateOf(TextFieldValue("")) }
    var stepGoal by remember { mutableStateOf(TextFieldValue("")) }

    // If there is data in db, fill the input fields
    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = TextFieldValue(it.name)
            age = TextFieldValue(it.age.toString())
            stepGoal = TextFieldValue(it.stepGoal.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = stepGoal,
            onValueChange = { stepGoal = it },
            label = { Text("Step Goal") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val newProfile = try {
                    com.example.mobilebetriebsysteme_android_app.data.UserProfile(
                        id = 0,
                        name = name.text,
                        age = age.text.toInt(),
                        stepGoal = stepGoal.text.toInt()
                    )
                } catch (e: NumberFormatException) {
                    null
                }

                scope.launch {
                    snackbarHostState.showSnackbar("Profile Saved!")
                }

                newProfile?.let {
                    scope.launch {
                        viewModel.saveProfile(it)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()

        ) {
            Text("Save")
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}