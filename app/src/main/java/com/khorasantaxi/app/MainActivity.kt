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
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Navy = Color(0xFF10233F)
private val Gold = Color(0xFFD7A94B)
private val Page = Color(0xFFF4F6FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize(), color = Page) { PassengerHome() }
            }
        }
    }
}

@Composable
private fun PassengerHome() {
    val context = LocalContext.current
    val cities = mapOf(
        "خراسان رضوی" to listOf("مشهد", "نیشابور", "سبزوار", "تربت‌حیدریه", "قوچان"),
        "خراسان شمالی" to listOf("بجنورد", "شیروان", "اسفراین", "آشخانه"),
        "خراسان جنوبی" to listOf("بیرجند", "قائن", "طبس", "فردوس", "نهبندان")
    )
    var province by remember { mutableStateOf("خراسان جنوبی") }
    var city by remember { mutableStateOf("بیرجند") }
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var originCoordinates by remember { mutableStateOf("") }
    var destinationCoordinates by remember { mutableStateOf("") }
    var locationMessage by remember { mutableStateOf("برای ثبت GPS، موقعیت مکانی گوشی را روشن کنید.") }
    var targetField by remember { mutableStateOf("origin") }

    fun applyLocation(location: Location) {
        val coordinates = String.format(Locale.US, "%.6f, %.6f", location.latitude, location.longitude)
        if (targetField == "origin") originCoordinates = coordinates else destinationCoordinates = coordinates
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(location.time))
        locationMessage = "موقعیت GPS ثبت شد • دقت حدود ${location.accuracy.toInt()} متر • $time"
    }

    fun locate() {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val last = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (last != null) applyLocation(last)
            if (!gpsEnabled) {
                locationMessage = "GPS خاموش است. آن را از تنظیمات سریع گوشی روشن کنید."
                return
            }
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    manager.removeUpdates(this)
                    applyLocation(location)
                }
                @Deprecated("Deprecated by Android")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
                override fun onProviderEnabled(provider: String) = Unit
                override fun onProviderDisabled(provider: String) = Unit
            }
            manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.getMainLooper())
            locationMessage = if (last == null) {
                "در حال دریافت موقعیت ماهواره‌ای GPS؛ ممکن است در فضای بسته کمی طول بکشد."
            } else {
                "موقعیت قبلی نمایش داده شد؛ در حال به‌روزرسانی با GPS..."
            }
        } catch (_: SecurityException) {
            locationMessage = "اجازه دسترسی به موقعیت مکانی داده نشده است."
        } catch (_: Exception) {
            locationMessage = "دریافت GPS ممکن نشد. GPS را روشن کنید و در فضای باز دوباره امتحان کنید."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) locate() else locationMessage = "برای ثبت موقعیت، اجازه مکان‌یابی لازم است."
    }

    fun requestLocation(field: String) {
        targetField = field
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) locate()
        else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    Column(
        Modifier.fillMaxSize().background(Page).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text("KHORASAN TAXI", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("تاکسی اینترنتی خراسان", color = Navy, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Text("سفر هوشمند در سه استان خراسان", color = Color.DarkGray)
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("استان و شهر", color = Navy, fontWeight = FontWeight.Bold)
                cities.keys.forEach { item ->
                    if (province == item) {
                        Button(onClick = { province = item; city = cities[item]!!.first() }, modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy)) { Text(item) }
                    } else {
                        OutlinedButton(onClick = { province = item; city = cities[item]!!.first() }, modifier = Modifier.fillMaxWidth()) { Text(item, color = Navy) }
                    }
                }
                Text("شهر یا محدوده نزدیک روستا", color = Navy, fontWeight = FontWeight.Bold)
                cities[province].orEmpty().forEach { item ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = city == item, onClick = { city = item })
                        Text(item, color = Navy)
                    }
                }
                Text("منطقه انتخابی: $city • $province", color = Color.DarkGray)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("مبدأ سفر", color = Navy, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = origin, onValueChange = { origin = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("نام روستا، محله یا نشانی مبدأ") },
                    placeholder = { Text("مثلاً روستای ...، بخش ...، شهرستان ...") })
                OutlinedButton(onClick = { requestLocation("origin") }, modifier = Modifier.fillMaxWidth()) {
                    Text("ثبت موقعیت فعلی برای مبدأ (GPS)")
                }
                OutlinedTextField(value = originCoordinates, onValueChange = { originCoordinates = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("مختصات مبدأ (عرض، طول جغرافیایی)") },
                    placeholder = { Text("32.000000, 59.000000") }, singleLine = true)
                HorizontalDivider()
                Text("مقصد سفر", color = Navy, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = destination, onValueChange = { destination = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("نام شهر، روستا یا نشانی مقصد") },
                    placeholder = { Text("نام مقصد را وارد کنید") })
                OutlinedButton(onClick = { requestLocation("destination") }, modifier = Modifier.fillMaxWidth()) {
                    Text("ثبت موقعیت فعلی برای مقصد (GPS)")
                }
                OutlinedTextField(value = destinationCoordinates, onValueChange = { destinationCoordinates = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("مختصات مقصد (عرض، طول جغرافیایی)") },
                    placeholder = { Text("32.000000, 59.000000") }, singleLine = true)
                Text(locationMessage, color = Color(0xFF526174), fontSize = 12.sp)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Navy), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("مناسب شهر و روستا", color = Color.White, fontWeight = FontWeight.Bold)
                Text("ثبت مختصات GPS و نوشتن نام روستا بدون نیاز به اینترنت انجام می‌شود. برای نمایش نقشه آفلاین، باید نقشه منطقه از قبل دانلود شده باشد.", color = Color.White.copy(alpha = 0.9f))
            }
        }
        Text("نسخه توسعه • ثبت سفر و اعزام راننده هنوز به سرور متصل نیست.", color = Color.Gray, fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(12.dp))
    }
}
