package com.example.mobilebetriebsysteme_android_app

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapScreen(context: Context) {
    // osmdroid init
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

    AndroidView(
        factory = {
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(52.52, 13.4050)) // Berlin
            }
        },
        update = { mapView ->
            // map updates
        }
    )
}
