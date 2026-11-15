package com.khorasantaxi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF10233F)
private val Gold = Color(0xFFD7A94B)
private val Page = Color(0xFFF4F6FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize(), color = Page) {
                    PassengerHome()
                }
            }
        }
    }
}

@Composable
private fun PassengerHome() {
    val cities = mapOf(
        "خراسان رضوی" to listOf("مشهد", "نیشابور", "سبزوار", "تربت‌حیدریه", "قوچان"),
        "خراسان شمالی" to listOf("بجنورد", "شیروان", "اسفراین", "آشخانه"),
        "خراسان جنوبی" to listOf("بیرجند", "قائن", "طبس", "فردوس", "نهبندان")
    )
    var province by remember { mutableStateOf("خراسان جنوبی") }
    var city by remember { mutableStateOf("بیرجند") }

    Column(
        Modifier.fillMaxSize().background(Page).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("KHORASAN TAXI", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("تاکسی اینترنتی خراسان", color = Navy, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Text("سفر هوشمند در سه استان خراسان", color = Color.DarkGray)
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("استان محل سفر", color = Navy, fontWeight = FontWeight.Bold)
                cities.keys.forEach { item ->
                    if (province == item) {
                        Button(onClick = { province = item; city = cities[item]!!.first() }, modifier = Modifier.fillMaxWidth()) {
                            Text(item)
                        }
                    } else {
                        OutlinedButton(onClick = { province = item; city = cities[item]!!.first() }, modifier = Modifier.fillMaxWidth()) {
                            Text(item, color = Navy)
                        }
                    }
                }
                Text("شهر", color = Navy, fontWeight = FontWeight.Bold)
                cities[province].orEmpty().forEach { item ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = city == item, onClick = { city = item })
                        Text(item, color = Navy)
                    }
                }
                Text("شهر انتخاب‌شده: $city", color = Color.DarkGray)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = Navy), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("سفر بعدی شما", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("انتخاب مبدأ و مقصد در مرحله بعد اضافه می‌شود.", color = Color.White)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                    Text("درخواست سفر به‌زودی", color = Navy, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text("نسخه توسعه اولیه • خدمات سفر هنوز فعال نیست", color = Color.Gray, fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
