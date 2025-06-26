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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.ProfileViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.ProfileViewModelFactory
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.WalkingSessionViewModel
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
fun MainPage(navController: NavController, walkingSessionViewModel: WalkingSessionViewModel) {
    val context = LocalContext.current

    // Profile ViewModel Factory & ViewModel
    val factory = remember { ProfileViewModelFactory(context.applicationContext as Application) }
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)

    // WalkingSession ViewModel
    val walkingSessionViewModel: WalkingSessionViewModel = viewModel()

    // Location permission
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var routeError by remember { mutableStateOf<String?>(null) }
    var destination by remember { mutableStateOf<GeoPoint?>(null) }
    val locationHistory = remember { mutableStateListOf<GeoPoint>() }
    val berlinLocation = GeoPoint(52.52, 13.4050) // Berlin başlangıç noktası

    val mapView = remember { createMapView(context) }
    var isDestinationLocked by remember { mutableStateOf(false) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isSearching by remember { mutableStateOf(false) }
    var showStatsPanel by remember { mutableStateOf(false) }

    // Get start location
    LaunchedEffect(true) {
        val location = getCurrentLocation(context)
        val startPoint = location?.let { GeoPoint(it.latitude, it.longitude) } ?: berlinLocation
        mapView.controller.setCenter(startPoint)
        addUserMarker(mapView, startPoint)
    }

    // Map click event
    val mapEventsReceiver = remember {
        object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p != null && !isDestinationLocked) {
                    destination = p
                    return true
                }
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }
    }

    DisposableEffect(mapView) {
        val overlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(overlay)
        onDispose {
            mapView.overlays.remove(overlay)
        }
    }

    // Location update, walk distance and route
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            while (true) {
                val location = getCurrentLocation(context)
                location?.let {
                    val point = GeoPoint(it.latitude, it.longitude)
                    locationHistory.add(point)

                    if (walkingSessionViewModel.isSessionActive.value && lastLocation != null) {
                        val result = FloatArray(1)
                        Location.distanceBetween(
                            lastLocation!!.latitude, lastLocation!!.longitude,
                            it.latitude, it.longitude,
                            result
                        )
                        val distanceInMeters = result[0]
                        walkingSessionViewModel.updateDistance(distanceInMeters)
                    }
                    lastLocation = it

                    mapView.post {
                        addUserMarker(mapView, point)
                        drawUserPath(mapView, locationHistory)
                    }
                }
                delay(3000)
            }
        }
    }

    // When a destination selected, start a session
    LaunchedEffect(destination) {
        destination?.let {
            mapView.post {
                addDestinationMarker(mapView, it)
            }
            walkingSessionViewModel.startSession()
            isDestinationLocked = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.matchParentSize())

        // Profile ve DualMode buttons - navigation with NavController
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                IconButton(
                    onClick = { navController.navigate("profile") },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ManageAccounts,
                        contentDescription = "Profile"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                IconButton(
                    onClick = { navController.navigate("dualmode") },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.VideogameAsset,
                        contentDescription = "Dual Mode"
                    )
                }
            }
        }

        // Tip bar for setting a destionation
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(
                    color = Color.White.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isDestinationLocked)
                    "Destination selected, to cancel the session click the x button!"
                else
                    "Set an location on the map by clicking to start an session!",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            IconButton(onClick = {
                isDestinationLocked = !isDestinationLocked
                if (!isDestinationLocked) {
                    walkingSessionViewModel.stopSession()
                }
            }) {
                Icon(
                    imageVector = if (isDestinationLocked) Icons.Default.Close else Icons.Default.LockOpen,
                    contentDescription = if (isDestinationLocked) "Destination locked" else "Destination editable",
                    tint = Color.Black
                )
            }
        }

        // Error message
        routeError?.let {
            Text(
                text = it,
                modifier = Modifier.align(Alignment.Center),
                color = Color.Red
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Go to current location FAB
        FloatingActionButton(
            onClick = {
                isSearching = true
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
                        isSearching = false
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.White,
            contentColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = "Go to location"
            )
        }

        if (isSearching) {
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
                        text = "Searching for location...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Stats panel toggle
        if (!showStatsPanel) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { showStatsPanel = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Show Stats",
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Click for stats",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Stats panel
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showStatsPanel = false }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Hide Stats"
                            )
                        }
                    }

                    Text("Walking Stats", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    WalkingStats(sessionViewModel = walkingSessionViewModel)
                }
            }
        }
    }
}

// ---- Helper Functions ----

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
    mapView.overlays.removeIf { it is Marker && (it.title == "My Location") }

    Marker(mapView).apply {
        position = location
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = "My Location"
        mapView.overlays.add(this)
    }
}

private fun addDestinationMarker(mapView: MapView, location: GeoPoint) {
    mapView.overlays.removeIf { it is Marker && (it.title == "Destination") }

    Marker(mapView).apply {
        position = location
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = "Destination"
        mapView.overlays.add(this)
    }
}

private fun drawUserPath(mapView: MapView, points: List<GeoPoint>) {
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
