package com.example.mobilebetriebsysteme_android_app.pages

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.ProfileViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.ProfileViewModelFactory
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.WalkingSessionViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.BluetoothViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@SuppressLint("MissingPermission")
@Composable
fun MainPage(
    navController: NavController,
    walkingSessionViewModel: WalkingSessionViewModel,
    bluetoothViewModel: BluetoothViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // ProfileViewModel creation using factory for user profile management
    val profileFactory = remember { ProfileViewModelFactory(context.applicationContext as Application) }
    val profileViewModel: ProfileViewModel = viewModel(factory = profileFactory)

    // WalkingSession ViewModel for session tracking (duration, distance, etc.)
    val walkingSessionVM: WalkingSessionViewModel = viewModel()

    // BluetoothViewModel for Bluetooth connection status and control
    val isBluetoothConnected by bluetoothViewModel.isConnected.collectAsState()

    // Check if location permission granted
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    // Error message for routing or other issues
    var routeErrorMessage by remember { mutableStateOf<String?>(null) }

    // Currently selected destination GeoPoint
    var destination by remember { mutableStateOf<GeoPoint?>(null) }

    // History of user's traveled GeoPoints (location tracking)
    val locationHistory = remember { mutableStateListOf<GeoPoint>() }

    // Default fallback location (Berlin)
    val defaultLocation = GeoPoint(52.52, 13.4050)

    // MapView instance for map display
    val mapView = remember { createMapView(context) }

    // Last known location, used to calculate incremental distance
    var lastKnownLocation by remember { mutableStateOf<Location?>(null) }

    // Dialog visibility flags
    var showStartSessionDialog by remember { mutableStateOf(false) }
    var showConfirmCancelDialog by remember { mutableStateOf(false) }

    // Pending destination user clicked on map but not confirmed yet
    var pendingDestination by remember { mutableStateOf<GeoPoint?>(null) }

    // Coroutine scope for async operations like showing snackbar
    val coroutineScope = rememberCoroutineScope()

    // For showing Snackbars (messages)
    val snackbarHostState = remember { SnackbarHostState() }

    // Flag to indicate if app is searching for location (loading state)
    var isSearchingLocation by remember { mutableStateOf(true) }

    // Flag to toggle walking stats panel visibility
    var showStatsPanel by remember { mutableStateOf(false) }

    // Dual mode checkbox state for starting dual mode session
    var dualModeChecked by remember { mutableStateOf(false) }

    // Location provider client for GPS data
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Session active state from WalkingSessionViewModel
    val isSessionActive by walkingSessionVM.isSessionActive.collectAsState()

    // Session duration in seconds (for display in message)
    val sessionDuration by walkingSessionVM.sessionDurationInSeconds.collectAsState()

    // Destination from WalkingSessionViewModel (in case it changes externally)
    val destinationPoint by walkingSessionVM.destinationPoint.collectAsState()

    // On first composition, move map to current or default location
    LaunchedEffect(Unit) {
        try {
            val location = getCurrentLocation(context)
            val startPoint = location?.let { GeoPoint(it.latitude, it.longitude) } ?: defaultLocation
            mapView.controller.setCenter(startPoint)
            addUserMarker(mapView, startPoint)
        } finally {
            isSearchingLocation = false
        }
    }

    // Automatically open stats panel when session starts, close when session ends
    LaunchedEffect(isSessionActive) {
        showStatsPanel = isSessionActive
    }

    // Map tap listener to select destination if session is NOT active
    val mapEventsReceiver = remember {
        object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p != null && !isSessionActive) {
                    pendingDestination = p
                    showStartSessionDialog = true
                    return true
                }
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }
    }

    // Attach and remove map tap listener overlay
    DisposableEffect(mapView) {
        val overlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(overlay)
        onDispose {
            mapView.overlays.remove(overlay)
        }
    }

    // Location updates: track user location every 3 seconds if permission granted
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            while (true) {
                val location = getCurrentLocation(context)
                location?.let {
                    val point = GeoPoint(it.latitude, it.longitude)
                    locationHistory.add(point)

                    if (walkingSessionVM.isSessionActive.value && lastKnownLocation != null) {
                        val result = FloatArray(1)
                        Location.distanceBetween(
                            lastKnownLocation!!.latitude, lastKnownLocation!!.longitude,
                            it.latitude, it.longitude,
                            result
                        )
                        val distanceInMeters = result[0]
                        walkingSessionVM.updateDistance(distanceInMeters)
                    }
                    lastKnownLocation = it

                    mapView.post {
                        addUserMarker(mapView, point)
                        drawUserPath(mapView, locationHistory)
                    }
                }
                delay(3000)
            }
        }
    }

    // Update destination marker on map when destination or session state changes
    LaunchedEffect(destinationPoint, isSessionActive) {
        mapView.post {
            // Remove previous destination markers
            mapView.overlays.removeIf { it is Marker && it.title == "Destination" }

            if (isSessionActive) {
                destinationPoint?.let { point ->
                    addDestinationMarker(mapView, point)
                }
            }
            mapView.invalidate()
        }
    }

    // UI Layout
    Box(Modifier.fillMaxSize()) {
        // Map display using AndroidView
        AndroidView(factory = { mapView }, modifier = Modifier.matchParentSize())

        // Profile and DualMode navigation buttons at top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                IconButton(
                    onClick = { navController.navigate("profile") },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                ) {
                    Icon(imageVector = Icons.Default.ManageAccounts, contentDescription = "Profile")
                }

                Spacer(modifier = Modifier.height(8.dp))

                IconButton(
                    onClick = { navController.navigate("dualmode") },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                ) {
                    Icon(imageVector = Icons.Default.VideogameAsset, contentDescription = "Dual Mode")
                }
            }
        }

        // Session status message at top center
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSessionActive) {
                Text(
                    text = "Session is started, have a good walk! Duration: ${sessionDuration / 60} min",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Set a location on the map by clicking to start a session!",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Confirm cancel session dialog
        if (showConfirmCancelDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmCancelDialog = false },
                title = { Text("Confirm Cancel") },
                text = { Text("Are you sure you want to cancel the session?") },
                confirmButton = {
                    TextButton(onClick = {
                        walkingSessionVM.stopSessionAndSave()
                        walkingSessionVM.clearDestination()
                        showConfirmCancelDialog = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmCancelDialog = false }) {
                        Text("No")
                    }
                }
            )
        }

        // Start session confirmation dialog
        if (showStartSessionDialog) {
            AlertDialog(
                onDismissRequest = {
                    showStartSessionDialog = false
                    pendingDestination = null
                    dualModeChecked = false
                },
                title = { Text("Start Session") },
                text = {
                    Column {
                        Text("Do you want to start a walking session at this location?")
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = dualModeChecked,
                                onCheckedChange = { dualModeChecked = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Start DualMode session? (A Competitive Mode, needs a Bluetooth connection!)")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (dualModeChecked && !isBluetoothConnected) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    "For DualMode a Bluetooth connection is needed, please go to DualPage and connect first!"
                                )
                            }
                            dualModeChecked = false
                        } else {
                            destination = pendingDestination
                            pendingDestination?.let { walkingSessionVM.setDestination(it) }
                            walkingSessionVM.startSession(dualModeChecked)
                            showStartSessionDialog = false
                            pendingDestination = null
                            dualModeChecked = false
                        }
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showStartSessionDialog = false
                        pendingDestination = null
                        dualModeChecked = false
                    }) {
                        Text("No")
                    }
                }
            )
        }

        // Error message display if any
        routeErrorMessage?.let {
            Text(
                text = it,
                modifier = Modifier.align(Alignment.Center),
                color = Color.Red
            )
        }

        // Snackbar host for showing short messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // FloatingActionButton to go to current location on map
        FloatingActionButton(
            onClick = {
                isSearchingLocation = true
                coroutineScope.launch {
                    try {
                        val location = fusedLocationClient
                            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .await()

                        if (location != null) {
                            val geoPoint = GeoPoint(location.latitude, location.longitude)
                            mapView.controller.setZoom(18.0)
                            mapView.controller.setCenter(geoPoint)
                        } else {
                            snackbarHostState.showSnackbar("Couldn't get location")
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error: ${e.message}")
                    } finally {
                        isSearchingLocation = false
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.White,
            contentColor = Color.Black
        ) {
            Icon(imageVector = Icons.Default.GpsFixed, contentDescription = "Go to location")
        }

        // Overlay loading indicator when searching for location
        if (isSearchingLocation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Getting your location...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Mini "Show stats" button when session active but panel is hidden
        if (isSessionActive && !showStatsPanel) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .clickable { showStatsPanel = true }
                    .padding(12.dp)
            ) {
                Text("Show stats", color = Color.Black)
            }
        }

        // Normal "Click for stats" button when session not active and panel hidden
        if (!isSessionActive && !showStatsPanel) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { showStatsPanel = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Show Stats", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Click for stats", color = Color.Black, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Stats panel showing walking stats and finish session button
        if (showStatsPanel) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 70.dp)
                    .fillMaxWidth(0.9f)
                    .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { showStatsPanel = false }) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Hide Stats")
                        }
                    }

                    Text("Walking Stats", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    WalkingStats(sessionViewModel = walkingSessionVM)

                    Spacer(Modifier.height(12.dp))

                    // Finish Session button to end current walking session
                    Button(
                        onClick = {
                            walkingSessionVM.stopSessionAndSave()
                            walkingSessionVM.clearDestination()
                            showStatsPanel = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Finish Session")
                    }
                }
            }
        }
    }
}

// -------- Helper Functions --------

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().apply {
        load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        userAgentValue = context.packageName
    }

    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        minZoomLevel = 5.0
        maxZoomLevel = 19.0
        controller.setZoom(15.0)
    }
}

private fun addUserMarker(mapView: MapView, location: GeoPoint) {
    // Remove previous user location marker if any
    mapView.overlays.removeIf { it is Marker && it.title == "My Location" }

    Marker(mapView).apply {
        position = location
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = "My Location"
        mapView.overlays.add(this)
    }
}

private fun addDestinationMarker(mapView: MapView, location: GeoPoint) {
    // Remove previous destination marker if any
    mapView.overlays.removeIf { it is Marker && it.title == "Destination" }

    Marker(mapView).apply {
        position = location
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = "Destination"
        mapView.overlays.add(this)
    }
}

private fun drawUserPath(mapView: MapView, points: List<GeoPoint>) {
    // Remove old user path polyline if any
    mapView.overlays.removeIf { it is Polyline && it.title == "UserPath" }

    if (points.size >= 2) {
        val polyline = Polyline().apply {
            setPoints(points)
            outlinePaint.strokeWidth = 6f
            outlinePaint.color = android.graphics.Color.GREEN
            title = "UserPath"
        }
        mapView.overlays.add(polyline)
        mapView.invalidate()
    }
}

@Composable
fun WalkingStats(sessionViewModel: WalkingSessionViewModel) {
    val time by sessionViewModel.sessionDurationInSeconds.collectAsState()
    val distance by sessionViewModel.distanceInMeters.collectAsState()

    Column(horizontalAlignment = Alignment.Start) {
        Text(text = "Duration: ${time / 60} min ${time % 60} sec")
        Text(text = "Distance: %.2f km".format(distance / 1000.0))
    }
}

@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(context: Context): Location? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000L
    ).build()

    return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        val callback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                if (result.locations.isNotEmpty()) {
                    cont.resume(result.locations[0], null)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, callback, null)

        cont.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}
