package com.example.myapplication.ui.theme.main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.theme.auth.AuthViewModel
import com.example.myapplication.ui.theme.auth.PredictionSection
import com.example.myapplication.ui.theme.navigation.AppBottomNavigationBar
import com.example.myapplication.ui.theme.navigation.NavigationItem
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RezervasyonEkrani(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var tarih by remember { mutableStateOf("") }
    var girisSaati by remember { mutableStateOf("") }
    var cikisSaati by remember { mutableStateOf("") }
    var pilDoluluk by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> tarih = "%02d/%02d/%04d".format(day, month + 1, year) },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePicker = { onTimeSelected: (String) -> Unit ->
        TimePickerDialog(context, { _, hour, minute ->
            onTimeSelected("%02d:%02d".format(hour, minute))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rezervasyon", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            AppBottomNavigationBar(
                navController = navController,
                items = listOf(
                    NavigationItem.NavigationScreen,
                    NavigationItem.Reservation,
                    NavigationItem.Profile
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rezervasyon Kartı
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EventNote, null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Rezervasyon Detayları", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    // Tarih Seçici
                    SelectionButton(
                        onClick = { datePickerDialog.show() },
                        icon = Icons.Default.CalendarToday,
                        label = if (tarih.isEmpty()) "Tarih Seç" else "Tarih: $tarih",
                        isSelected = tarih.isNotEmpty()
                    )

                    // Giriş Saati
                    SelectionButton(
                        onClick = { timePicker { girisSaati = it } },
                        icon = Icons.Default.AccessTime,
                        label = if (girisSaati.isEmpty()) "Giriş Saati Seç" else "Giriş: $girisSaati",
                        isSelected = girisSaati.isNotEmpty()
                    )

                    // Çıkış Saati
                    SelectionButton(
                        onClick = { timePicker { cikisSaati = it } },
                        icon = Icons.Default.AccessTime,
                        label = if (cikisSaati.isEmpty()) "Çıkış Saati Seç" else "Çıkış: $cikisSaati",
                        isSelected = cikisSaati.isNotEmpty()
                    )

                    // Pil Doluluk
                    OutlinedTextField(
                        value = pilDoluluk,
                        onValueChange = { pilDoluluk = it },
                        label = { Text("Pil Doluluk Oranı (%)") },
                        leadingIcon = { Icon(Icons.Default.BatteryChargingFull, null, tint = Secondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            cursorColor = Primary
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Rezervasyon Butonu
            Button(
                onClick = { navController.navigate(NavigationItem.Payment.route) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Rezervasyon Yap", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))

            // Yoğunluk Tahmini Bölümü
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Analytics, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Yoğunluk Tahmini", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Spacer(Modifier.height(12.dp))
                    PredictionSection(
                        viewModel = viewModel,
                        tarih = tarih,
                        girisSaati = girisSaati
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SelectionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    isSelected: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.08f) else Color.Transparent,
            contentColor = if (isSelected) Primary else TextSecondary
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = if (isSelected) 2.dp else 1.dp
        )
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal)
    }
}
