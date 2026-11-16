package com.khorasantaxi.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.content.Context
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions
import java.util.Locale

private const val KHORASAN_MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"
private val MapNavy = Color(0xFF10233F)
private val MapGold = Color(0xFFD7A94B)

@Composable
fun MapPickerScreen(
    title: String,
    initialCoordinates: String?,
    onConfirm: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedCoordinates by remember { mutableStateOf(initialCoordinates.orEmpty()) }
    var mapMessage by remember { mutableStateOf("نقشه در حال بارگذاری است...") }

    val initialPoint = remember(initialCoordinates) { parseCoordinates(initialCoordinates) }
    val mapView = remember(context) {
        MapLibre.getInstance(context.applicationContext)
        MapView(context).also { it.onCreate(null) }
    }

    DisposableEffect(mapView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        LaunchedEffect(mapView, initialPoint) {
            mapView.getMapAsync { map ->
                val start = initialPoint ?: LatLng(32.8597, 59.2211)
                map.setStyle(Style.Builder().fromUri(KHORASAN_MAP_STYLE)) {
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().target(start).zoom(if (initialPoint == null) 11.5 else 15.0).build()
                    ))
                    if (initialPoint != null) {
                        map.addMarker(MarkerOptions().position(initialPoint).title("نقطه انتخاب‌شده"))
                    }
                    map.addOnMapClickListener { point ->
                        selectedCoordinates = String.format(Locale.US, "%.6f, %.6f", point.latitude, point.longitude)
                        map.clear()
                        map.addMarker(MarkerOptions().position(point).title("مبدأ یا مقصد"))
                        mapMessage = "نقطه انتخاب شد؛ برای جابه‌جایی، جای دیگری از نقشه را لمس کنید."
                        true
                    }
                    mapMessage = "نقشه آماده است؛ برای انتخاب نقطه، روی نقشه لمس کنید."
                }
            }
        }

        Column(
            Modifier.align(Alignment.TopCenter).fillMaxWidth()
                .background(MapNavy.copy(alpha = 0.94f)).padding(14.dp)
        ) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(mapMessage, color = Color.White.copy(alpha = 0.88f), style = MaterialTheme.typography.bodySmall)
        }

        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Color.White.copy(alpha = 0.97f))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                if (selectedCoordinates.isBlank()) "هنوز نقطه‌ای انتخاب نشده است" else "مختصات انتخابی: $selectedCoordinates",
                color = MapNavy,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("بازگشت") }
                Button(
                    onClick = { if (selectedCoordinates.isNotBlank()) onConfirm(selectedCoordinates) },
                    enabled = selectedCoordinates.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MapNavy)
                ) { Text("ثبت این نقطه") }
            }
            Text("نقشه آنلاین • OpenFreeMap / OpenStreetMap", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun parseCoordinates(value: String?): LatLng? {
    if (value.isNullOrBlank()) return null
    val parts = value.split(",")
    if (parts.size != 2) return null
    val latitude = parts[0].trim().toDoubleOrNull() ?: return null
    val longitude = parts[1].trim().toDoubleOrNull() ?: return null
    if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
    return LatLng(latitude, longitude)
}
