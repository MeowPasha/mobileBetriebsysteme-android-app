package com.example.mobilebetriebsysteme_android_app.pages

import android.app.Application
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilebetriebsysteme_android_app.data.profile.UserProfile
import com.example.mobilebetriebsysteme_android_app.data.session.WalkingSessionEntity
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.profileVM.ProfileViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.walkingSessionVM.WalkingSessionViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.walkingSessionVM.WalkingSessionViewModelFactory
import kotlinx.coroutines.launch

data class WalkingSession(val durationSeconds: Int, val distanceMeters: Float)

@Composable
fun ProfilePage(
    viewModel: ProfileViewModel,
    walkingSessionViewModel: WalkingSessionViewModel,
    onClose: () -> Unit
) {
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
            height = TextFieldValue(it.height.toString())
            stepGoal = TextFieldValue(it.stepGoal.toString())
        }
    }

    val displayName = if (name.text.isNotBlank()) name.text else "User"

    val context = LocalContext.current
    val sessionViewModel: WalkingSessionViewModel = viewModel(
        factory = WalkingSessionViewModelFactory(context.applicationContext as Application)
    )

    val allSessions by sessionViewModel.allSessions.collectAsState<List<WalkingSessionEntity>, List<WalkingSessionEntity>>(initial = emptyList())
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Üst kısım profile içeriği
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
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

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val newProfile = try {
                            UserProfile(
                                id = 0,
                                name = name.text,
                                age = age.text.toInt(),
                                height = height.text.toInt(),
                                stepGoal = stepGoal.text.toInt(),
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

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Sessions",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(allSessions) { session ->
                    SessionItem(
                        session = session,
                        onDelete = { sessionToDelete ->
                            scope.launch {
                                walkingSessionViewModel.deleteSession(sessionToDelete)
                                snackbarHostState.showSnackbar("Session deleted")
                            }
                        },
                        calculateSteps = walkingSessionViewModel::calculateSteps
                    )
                }
            }
        }
    }
}

@Composable
fun SessionItem(
    session: WalkingSessionEntity,
    onDelete: (WalkingSessionEntity) -> Unit,
    calculateSteps: (Float) -> Int
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Duration: ${session.durationSeconds / 60} min ${session.durationSeconds % 60} sec")
                Text(text = "Distance: %.2f km".format(session.distanceMeters / 1000))
                Text(text = "Steps: ${calculateSteps(session.distanceMeters)}")
                Text(text = "Date: ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(java.util.Date(session.timestamp))}")
            }

            IconButton(onClick = { showConfirmDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Session",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this session?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(session)
                    showConfirmDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}



