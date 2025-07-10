package com.example.mobilebetriebsysteme_android_app.pages

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilebetriebsysteme_android_app.data.profile.UserProfile
import com.example.mobilebetriebsysteme_android_app.data.session.WalkingSessionEntity
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.BluetoothViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.profileVM.ProfileViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.walkingSessionVM.WalkingSessionViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.walkingSessionVM.WalkingSessionViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProfilePage(
    viewModel: ProfileViewModel,
    walkingSessionViewModel: WalkingSessionViewModel,
    bluetoothViewModel: BluetoothViewModel,
    onClose: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val userProfile by viewModel.profile.collectAsState()

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var age by remember { mutableStateOf(TextFieldValue("")) }
    var stepGoal by remember { mutableStateOf(TextFieldValue("")) }
    var height by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = TextFieldValue(it.name)
            age = TextFieldValue(it.age.toString())
            height = TextFieldValue(it.height.toString())
            stepGoal = TextFieldValue(it.stepGoal.toString())
        }
    }

    fun validateInputs(): Boolean {
        return name.text.isNotBlank()
                && age.text.toIntOrNull() != null
                && height.text.toIntOrNull() != null
                && stepGoal.text.toIntOrNull() != null
    }

    val displayName = if (name.text.isNotBlank()) name.text else "User"
    val context = LocalContext.current
    val sessionViewModel: WalkingSessionViewModel = viewModel(
        factory = WalkingSessionViewModelFactory(context.applicationContext as Application)
    )
    val allSessions by sessionViewModel.allSessions.collectAsState(initial = emptyList())

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth > 600.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Hi $displayName!\nWelcome to your Profile Page",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
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
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = name.text.ifBlank { "Name" },
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(text = "Age: ${age.text.ifBlank { "--" }}")
                            Text(text = "Height: ${height.text.ifBlank { "--" }} cm")
                        }
                    }
                }
                item {
                    if (isTablet) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height (cm)") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(value = stepGoal, onValueChange = { stepGoal = it }, label = { Text("Step Goal") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                            }
                        }
                    } else {
                        Column {
                            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height (cm)") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = stepGoal, onValueChange = { stepGoal = it }, label = { Text("Step Goal") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
                        }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = {
                            if (!validateInputs()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please fill in all fields correctly before saving.")
                                }
                                return@Button
                            }
                            val newProfile = try {
                                UserProfile(0, name.text, age.text.toInt(), height.text.toInt(), stepGoal.text.toInt())
                            } catch (e: NumberFormatException) {
                                null
                            }
                            newProfile?.let {
                                scope.launch {
                                    viewModel.saveProfile(it)
                                    snackbarHostState.showSnackbar("Profile saved!")
                                }
                            }
                        }, modifier = Modifier.weight(1f)) {
                            Text("Save")
                        }
                        OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                            Text("Close")
                        }
                    }
                }
                item {
                    Text("Sessions", style = MaterialTheme.typography.headlineMedium)
                }
                items(allSessions) { session ->
                    SessionItem(
                        session = session,
                        walkingSessionViewModel = walkingSessionViewModel,
                        bluetoothViewModel = bluetoothViewModel,
                        onDelete = {
                            scope.launch {
                                walkingSessionViewModel.deleteSession(it)
                                snackbarHostState.showSnackbar("Session deleted")
                            }
                        },
                        calculateSteps = walkingSessionViewModel::calculateSteps,
                    )
                }
            }
        }
    }
}

@Composable
fun SessionItem(
    session: WalkingSessionEntity,
    walkingSessionViewModel: WalkingSessionViewModel,
    bluetoothViewModel: BluetoothViewModel,
    onDelete: (WalkingSessionEntity) -> Unit,
    calculateSteps: (Float) -> Int,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showConfirmDialogDM by remember { mutableStateOf(false) }
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
                Text("Duration: ${session.durationSeconds / 60} min ${session.durationSeconds % 60} sec")
                Text("Distance: %.2f km".format(session.distanceMeters / 1000))
                Text("Steps: ${calculateSteps(session.distanceMeters)}")
                Text("Date: ${SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date(session.timestamp))}")

                if (session.isDualMode) {
//                    Text("Already Compared?: ${if (session.isCompared) "Yes" else "No"}")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (session.isDualMode) {
                    Icon(
                        imageVector = Icons.Default.VideogameAsset,
                        contentDescription = "Dual Mode Session",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if(session.isDualMode && !session.isCompared) {
//                    IconButton(onClick = { showConfirmDialogDM = true }) {
//                        Icon(
//                            imageVector = Icons.Default.Compare,
//                            contentDescription = "Compare Session",
//                            tint = MaterialTheme.colorScheme.secondary
//                        )
//                    }
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
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("No") }
            }
        )
    }

    if (showConfirmDialogDM) {
        AlertDialog(
            onDismissRequest = { showConfirmDialogDM = false },
            title = { Text("Confirm Compare") },
            text = { Text("Do you want to start a comparison with this session?") },
            confirmButton = {
                TextButton(onClick = {
                    walkingSessionViewModel.compareSessions(session, bluetoothViewModel)
                    showConfirmDialogDM = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialogDM = false }) { Text("No") }
            }
        )
    }
}
