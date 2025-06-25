package com.example.mobilebetriebsysteme_android_app.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.mobilebetriebsysteme_android_app.data.UserProfile
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfilePage(viewModel: ProfileViewModel, onClose: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val userProfile by viewModel.profile.collectAsState()

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var age by remember { mutableStateOf(TextFieldValue("")) }
    var stepGoal by remember { mutableStateOf(TextFieldValue("")) }
    var height by remember { mutableStateOf(TextFieldValue("")) }
    var avgSpeed by remember { mutableStateOf("0.0 km/h") }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = TextFieldValue(it.name)
            age = TextFieldValue(it.age.toString())
            stepGoal = TextFieldValue(it.stepGoal.toString())
        }
    }

    val displayName = if (name.text.isNotBlank()) name.text else "User"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Modern ve ortalanmış hoşgeldin mesajı
            Text(
                text = "Hi $displayName!\nWelcome to your Profile Page",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp)
                    .padding(bottom = 32.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )

            // Profil resmi + bilgiler Row’u
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary
                    )


                }

                Spacer(modifier = Modifier.width(24.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = name.text.ifBlank { "Name" },
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Age: ${age.text.ifBlank { "--" }}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Height: ${height.text.ifBlank { "--" }} cm",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = stepGoal,
                onValueChange = { stepGoal = it },
                label = { Text("Step Goal") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Average Speed: $avgSpeed",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Close")
            }
        }
    }
}

