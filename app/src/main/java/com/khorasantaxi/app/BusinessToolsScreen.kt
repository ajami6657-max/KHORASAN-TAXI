package com.khorasantaxi.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ToolsNavy = Color(0xFF10233F)
private val ToolsGold = Color(0xFFD7A94B)

@Composable
fun BusinessToolsScreen(onBack: () -> Unit) {
    var fareText by remember { mutableStateOf("100000") }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("سلام! من راهنمای اولیه تاکسی خراسان هستم. درباره پرداخت، کمیسیون، سفر یا پشتیبانی سؤال کن.") }
    val fare = fareText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val commission = fare / 5L
    val driverShare = fare - commission

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text("بازگشت") }
        Text("پرداخت و پشتیبانی تاکسی خراسان", color = ToolsNavy, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)

        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("مدیریت سهم هر سفر", color = ToolsNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("مبلغ کرایه (تومان)")
                OutlinedTextField(
                    value = fareText,
                    onValueChange = { fareText = it.filter { ch -> ch.isDigit() }.take(12) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("مثلاً ۱۰۰۰۰۰") }
                )
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("سهم پلتفرم (۲۰٪)")
                    Text("%,d تومان".format(commission), color = ToolsGold, fontWeight = FontWeight.Bold)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("سهم راننده (۸۰٪)")
                    Text("%,d تومان".format(driverShare), color = ToolsNavy, fontWeight = FontWeight.Bold)
                }
                Text("نمونه محاسبه است؛ برداشت کمیسیون و پرداخت واقعی پس از اتصال درگاه و بک‌اند امن فعال می‌شود.", color = Color(0xFF8A5A00), fontSize = 12.sp)
            }
        }

        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("پشتیبانی هوشمند", color = ToolsNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("راهنمای اولیه آفلاین • پاسخ‌گویی فارسی")
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("سؤال یا مشکل خود را بنویسید") },
                    minLines = 2
                )
                Button(
                    onClick = {
                        val q = question.trim()
                        answer = when {
                            q.isBlank() -> "لطفاً سؤال خود را بنویسید."
                            listOf("پرداخت", "پول", "درگاه", "کارت").any { q.contains(it) } ->
                                "برای پیگیری پرداخت، شماره پیگیری و شناسه سفر را نگه دارید. در نسخه فعلی پرداخت آنلاین هنوز فعال نیست."
                            listOf("کمیسیون", "۲۰", "درصد", "سهم").any { q.contains(it) } ->
                                "طبق تنظیم فعلی، ۲۰ درصد کرایه سهم پلتفرم و ۸۰ درصد سهم راننده است. محاسبه واقعی باید در سرور انجام و ثبت شود."
                            listOf("راننده", "پیدا", "سفر", "تأخیر").any { q.contains(it) } ->
                                "برای پیگیری سفر، کد سفر، زمان و مبدأ و مقصد را آماده کنید. اعزام راننده هنوز به سرور متصل نشده است."
                            listOf("لغو", "کنسل", "بازگشت").any { q.contains(it) } ->
                                "قوانین لغو و بازپرداخت باید پیش از فعال‌سازی پرداخت در سامانه ثبت شود. فعلاً تراکنش واقعی انجام نمی‌شود."
                            else -> "درخواست شما ثبت محلی نمی‌شود و به اپراتور ارسال نشده است. این نسخه پاسخ‌گوی اولیه آفلاین است؛ برای رسیدگی انسانی باید سامانه پشتیبانی به سرور متصل شود."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ToolsNavy)
                ) { Text("دریافت پاسخ") }
                Text("پاسخ راهنما:", fontWeight = FontWeight.Bold, color = ToolsNavy)
                Text(answer, color = Color.DarkGray)
                Text("این نسخه دستیار FAQ ساده است، نه مدل هوش مصنوعی متصل یا مولد؛ اتصال مدل واقعی و انتقال گفتگو به پشتیبان انسانی در مرحله سرور انجام می‌شود.", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
