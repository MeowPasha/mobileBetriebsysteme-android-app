package com.example.mobilebetriebsysteme_android_app.pages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.mobilebetriebsysteme_android_app.viewmodel.ProfileViewModel


@Composable
fun ProfilePage(viewModel: ProfileViewModel) {
    val profile by viewModel.profileState.collectAsState()

    var name by remember { mutableStateOf(TextFieldValue(profile?.name ?: "")) }
    var age by remember { mutableStateOf(TextFieldValue(profile?.age?.toString() ?: "")) }
    var stepGoal by remember { mutableStateOf(TextFieldValue(profile?.stepGoal?.toString() ?: "")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        TextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))

        TextField(
            value = stepGoal,
            onValueChange = { stepGoal = it },
            label = { Text("Step Goal") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveProfile(
                    name = name.text,
                    age = age.text.toIntOrNull() ?: 0,
                    stepGoal = stepGoal.text.toIntOrNull() ?: 0
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}