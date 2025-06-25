package com.example.mobilebetriebsysteme_android_app.pages

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.ProfileViewModel
import com.example.mobilebetriebsysteme_android_app.presentation.viewmodel.ProfileViewModelFactory
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
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
fun MainPage() {
    val context = LocalContext.current

    // ProfilePage:
    // ViewModelFactory instance
    val factory = remember { ProfileViewModelFactory(context.applicationContext as Application) }

    // Get ViewModel with factory
    val viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

    var showProfile by remember { mutableStateOf(false) }
    if (showProfile) {
        ProfilePage(viewModel = viewModel, onClose = {showProfile == false})
        return
    }

    // DualMode:
    var showDualMode by remember {mutableStateOf(false)}
    if (showDualMode) {
        DualModePage(onClose = { showDualMode = false })
        return
    }

    // Panel:
    var showStatsPanel by remember { mutableStateOf(false) }

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

    val berlinLocation = GeoPoint(52.52, 13.4050) // Berlin

    val mapView = remember { createMapView(context) }
    var isDestinationLocked by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(true) {
        val location = getCurrentLocation(context)
        val startPoint = location?.let { GeoPoint(it.latitude, it.longitude) } ?: berlinLocation
        mapView.controller.setCenter(startPoint)
        addUserMarker(mapView, startPoint)
    }

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

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            while (true) {
                val location = getCurrentLocation(context)
                location?.let {
                    val point = GeoPoint(it.latitude, it.longitude)
                    locationHistory.add(point)
                    mapView.post {
                        addUserMarker(mapView, point)
                        drawUserPath(mapView, locationHistory)
                    }
                }
                delay(3000)
            }
        }
    }

    LaunchedEffect(destination) {
        destination?.let {
            mapView.post {
                addDestinationMarker(mapView, it)
            }
        }
        isDestinationLocked = true
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.matchParentSize())

        // Profile button
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
                    onClick = { showProfile = true },
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
                    onClick = { showDualMode = true },
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
                    "Click lock button to unlock destination edit!"
                else
                    "Select your destination by tapping on the map!",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )


            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            IconButton(onClick = { isDestinationLocked = !isDestinationLocked }) {
                Icon(
                    imageVector = if (isDestinationLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (isDestinationLocked) "Destination locked" else "Destination editable",
                    tint = Color.Black
                )
            }
        }

        routeError?.let {
            Text(
                text = it,
                modifier = Modifier.align(Alignment.Center),
                color = Color.Red
            )
        }

        // 🔽 Arrow button
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



        // 🧾 Mini panel
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
                    // Close button
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
                    Text("Steps: 1234")
                    Text("Duration: 45 minutes")
                    Text("Distance: 2.5 km")
                }
            }
        }
    }
}

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

@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(context: Context): Location? = withContext(Dispatchers.Main) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
        1000L
    ).build()

    suspendCancellableCoroutine<Location?> { cont ->
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
