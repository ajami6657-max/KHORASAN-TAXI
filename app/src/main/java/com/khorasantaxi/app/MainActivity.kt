package com.khorasantaxi.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

private val TaxiNavy = Color(0xFF10233F)
private val TaxiGold = Color(0xFFD7A94B)
private val TaxiPage = Color(0xFFF4F6FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize(), color = TaxiPage) {
                    TaxiRoot()
                }
            }
        }
    }
}

@Composable
private fun TaxiRoot() {
    var showBusinessTools by remember { mutableStateOf(false) }
    if (showBusinessTools) {
        BackHandler { showBusinessTools = false }
        BusinessToolsScreen(onBack = { showBusinessTools = false })
    } else {
        RideMapScreen(onOpenBusinessTools = { showBusinessTools = true })
    }
}

@Composable
private fun RideMapScreen(onOpenBusinessTools: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("ride_draft", Context.MODE_PRIVATE) }
    var activeField by remember { mutableStateOf("origin") }
    var origin by remember { mutableStateOf(prefs.getString("origin", "") ?: "") }
    var destination by remember { mutableStateOf(prefs.getString("destination", "") ?: "") }
    var originCoordinates by remember { mutableStateOf(prefs.getString("origin_coordinates", "") ?: "") }
    var destinationCoordinates by remember { mutableStateOf(prefs.getString("destination_coordinates", "") ?: "") }
    var status by remember { mutableStateOf("مبدأ را روی نقشه انتخاب کنید یا موقعیت فعلی را بزنید.") }
    var showReview by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        RideMapCanvas(
            originCoordinates = originCoordinates,
            destinationCoordinates = destinationCoordinates,
            onMapPointSelected = { coordinates ->
                if (activeField == "origin") {
                    originCoordinates = coordinates
                    prefs.edit().putString("origin_coordinates", coordinates).apply()
                    status = "مبدأ روی نقشه ثبت شد؛ حالا مقصد را انتخاب کنید."
                    activeField = "destination"
                } else {
                    destinationCoordinates = coordinates
                    prefs.edit().putString("destination_coordinates", coordinates).apply()
                    status = "مقصد روی نقشه ثبت شد."
                }
            },
            onGpsLocation = { coordinates ->
                originCoordinates = coordinates
                prefs.edit().putString("origin_coordinates", coordinates).apply()
                status = "موقعیت فعلی شما به‌عنوان مبدأ ثبت شد."
            },
            onStatus = { status = it }
        )

        Row(
            Modifier.align(Alignment.TopCenter).fillMaxWidth()
                .background(TaxiNavy.copy(alpha = 0.96f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("KHORASAN TAXI", color = TaxiGold, style = MaterialTheme.typography.labelLarge)
                Text("تاکسی اینترنتی خراسان", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            TextButton(onClick = onOpenBusinessTools) {
                Text("پرداخت و پشتیبانی", color = Color.White)
            }
        }

        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Color.White.copy(alpha = 0.98f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("درخواست خودرو", color = TaxiNavy, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = activeField == "origin",
                    onClick = { activeField = "origin"; status = "روی نقشه لمس کنید تا مبدأ مشخص شود." },
                    label = { Text("انتخاب مبدأ") }
                )
                FilterChip(
                    selected = activeField == "destination",
                    onClick = { activeField = "destination"; status = "روی نقشه لمس کنید تا مقصد مشخص شود." },
                    label = { Text("انتخاب مقصد") }
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { activeField = "origin" }) {
                    Text("GPS من", color = TaxiNavy)
                }
            }
            OutlinedTextField(
                value = origin,
                onValueChange = { origin = it; prefs.edit().putString("origin", it).apply() },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("نشانی مبدأ (اختیاری؛ شهر یا روستا)") },
                placeholder = { Text("مثلاً روستای ...، خیابان ...") }
            )
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it; prefs.edit().putString("destination", it).apply() },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("نشانی مقصد (اختیاری)") },
                placeholder = { Text("نام مقصد یا نشانی دقیق") }
            )
            Text(
                "مبدأ: ${if (originCoordinates.isBlank()) "روی نقشه انتخاب نشده" else originCoordinates}   •   مقصد: ${if (destinationCoordinates.isBlank()) "انتخاب نشده" else destinationCoordinates}",
                color = TaxiNavy,
                style = MaterialTheme.typography.bodySmall
            )
            Text(status, color = Color(0xFF586579), style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = { showReview = true },
                enabled = originCoordinates.isNotBlank() && destinationCoordinates.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TaxiNavy)
            ) {
                Text("درخواست خودرو", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                "انتخاب نقشه و GPS فعال است؛ اعزام راننده پس از اتصال سرور عملیاتی می‌شود.",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }

    if (showReview) {
        AlertDialog(
            onDismissRequest = { showReview = false },
            title = { Text("بررسی مبدأ و مقصد") },
            text = {
                Text("مبدأ: $originCoordinates\n$origin\n\nمقصد: $destinationCoordinates\n$destination\n\nاین اطلاعات در گوشی ذخیره شده است. برای ارسال درخواست واقعی، سرور و سامانه اعزام راننده هنوز باید متصل شوند.")
            },
            confirmButton = {
                TextButton(onClick = { showReview = false }) { Text("متوجه شدم") }
            }
        )
    }
}

@Composable
private fun RideMapCanvas(
    originCoordinates: String,
    destinationCoordinates: String,
    onMapPointSelected: (String) -> Unit,
    onGpsLocation: (String) -> Unit,
    onStatus: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var nativeMap by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
    var gpsPending by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            gpsPending = true
        } else {
            onStatus("برای نمایش موقعیت فعلی، اجازه دسترسی به مکان را فعال کنید.")
        }
    }

    val mapView = remember(context) {
        org.maplibre.android.MapLibre.getInstance(context.applicationContext)
        org.maplibre.android.maps.MapView(context).also { it.onCreate(null) }
    }

    DisposableEffect(mapView, lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_START -> mapView.onStart()
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> mapView.onResume()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) mapView.onStart()
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) mapView.onResume()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(gpsPending) {
        if (!gpsPending) return@LaunchedEffect
        gpsPending = false
        val fine = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val provider = if (fine) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
        try {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(provider)) {
                onStatus(if (provider == LocationManager.GPS_PROVIDER) "GPS گوشی خاموش است؛ آن را روشن کنید." else "مکان‌یابی گوشی در دسترس نیست.")
                return@LaunchedEffect
            }
            val last = if (fine || coarse) manager.getLastKnownLocation(provider) else null
            fun deliver(location: Location) {
                val coords = String.format(java.util.Locale.US, "%.6f, %.6f", location.latitude, location.longitude)
                onGpsLocation(coords)
                nativeMap?.animateCamera(
                    org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                        org.maplibre.android.geometry.LatLng(location.latitude, location.longitude), 16.0
                    )
                )
            }
            if (last != null) deliver(last)
            val listener = object : android.location.LocationListener {
                override fun onLocationChanged(location: Location) {
                    deliver(location)
                    manager.removeUpdates(this)
                }
                @Deprecated("Deprecated by Android")
                override fun onStatusChanged(providerName: String?, statusCode: Int, extras: Bundle?) = Unit
                override fun onProviderEnabled(providerName: String) = Unit
                override fun onProviderDisabled(providerName: String) = Unit
            }
            manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            onStatus(if (last == null) "در حال دریافت موقعیت GPS..." else "موقعیت قبلی پیدا شد؛ در حال تازه‌سازی GPS...")
        } catch (_: SecurityException) {
            onStatus("دسترسی GPS داده نشده است.")
        } catch (_: Exception) {
            onStatus("موقعیت دریافت نشد؛ GPS را روشن کنید و دوباره بزنید.")
        }
    }

    Box(Modifier.fillMaxSize()) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
        LaunchedEffect(mapView) {
            mapView.getMapAsync { map ->
                nativeMap = map
                val birjand = org.maplibre.android.geometry.LatLng(32.8597, 59.2211)
                map.setStyle(org.maplibre.android.maps.Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty")) {
                    map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                        .target(birjand).zoom(12.0).build()
                    map.addOnMapClickListener { point ->
                        onMapPointSelected(String.format(java.util.Locale.US, "%.6f, %.6f", point.latitude, point.longitude))
                        true
                    }
                    onStatus("نقشه آماده است؛ برای انتخاب نقطه، روی نقشه لمس کنید.")
                }
            }
        }
        LaunchedEffect(nativeMap, originCoordinates, destinationCoordinates) {
            val map = nativeMap ?: return@LaunchedEffect
            val originPoint = parseRideCoordinates(originCoordinates)
            val destinationPoint = parseRideCoordinates(destinationCoordinates)
            map.clear()
            if (originPoint != null) {
                map.addMarker(org.maplibre.android.annotations.MarkerOptions().position(originPoint).title("مبدأ"))
            }
            if (destinationPoint != null) {
                map.addMarker(org.maplibre.android.annotations.MarkerOptions().position(destinationPoint).title("مقصد"))
            }
        }
        FloatingActionButton(
            onClick = {
                val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (fine || coarse) gpsPending = true
                else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp, bottom = 300.dp),
            containerColor = Color.White,
            contentColor = TaxiNavy
        ) {
            Text("⌖", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

private fun parseRideCoordinates(value: String): org.maplibre.android.geometry.LatLng? {
    if (value.isBlank()) return null
    val parts = value.split(",")
    if (parts.size != 2) return null
    val lat = parts[0].trim().toDoubleOrNull() ?: return null
    val lon = parts[1].trim().toDoubleOrNull() ?: return null
    if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return null
    return org.maplibre.android.geometry.LatLng(lat, lon)
}
