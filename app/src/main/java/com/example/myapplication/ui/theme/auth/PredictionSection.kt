package com.example.myapplication.ui.theme.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@Composable
fun PredictionSection(
    viewModel: AuthViewModel,
    tarih: String,
    girisSaati: String
) {
    val ui by viewModel.predictionUi.collectAsState()
    val isLoading by viewModel.predictionLoading.collectAsState()
    var attempted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tarih, girisSaati) {
        errorMessage = null
        attempted = false
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = {
                when {
                    tarih.isEmpty() || girisSaati.isEmpty() -> {
                        errorMessage = "Lütfen tarih ve saat seçin"
                        attempted = true
                    }
                    else -> {
                        val hourInt = girisSaati.substringBefore(":").trim().toIntOrNull()
                        if (hourInt != null) {
                            viewModel.runPredictionForSingle(tarih, hourInt)
                            attempted = true
                            errorMessage = null
                        } else {
                            errorMessage = "Geçersiz saat formatı"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(Icons.Default.TrendingUp, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Yoğunluk Tahmini Yap", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(14.dp))

        errorMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(msg, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.height(8.dp))
        }

        when {
            isLoading -> CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
            ui != null -> PredictionResultCard(ui!!, tarih, girisSaati)
            attempted && !isLoading ->
                Text("Tahmin alınamadı!", color = Error, fontSize = 14.sp)
            else ->
                Text("Tarih ve saat seçin, ardından tahmin yapın.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun PredictionResultCard(
    prediction: PredictionUiState,
    tarih: String,
    girisSaati: String
) {
    val (densityText, densityColor, densityIcon) = when (prediction.density) {
        "DÜŞÜK" -> Triple("DÜŞÜK", DensityLow, Icons.Default.CheckCircle)
        "ORTA" -> Triple("ORTA", DensityMedium, Icons.Default.Warning)
        else -> Triple("YÜKSEK", DensityHigh, Icons.Default.Error)
    }

    val sourceLabel = when (prediction.source) {
        PredictionSource.API -> "Sunucu (API)"
        PredictionSource.LOCAL -> "Cihaz (Offline)"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = densityColor.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(densityIcon, null, tint = densityColor, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Yoğunluk: $densityText", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = densityColor)
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(icon = Icons.Default.DirectionsCar, value = "${prediction.vehicleCount}", label = "Araç")
                StatItem(icon = Icons.Default.Timer, value = "${prediction.avgParkMinutes} dk", label = "Ort. Park")
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$tarih  $girisSaati", fontSize = 12.sp, color = TextSecondary)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (prediction.source == PredictionSource.API) Primary.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
                ) {
                    Text(
                        sourceLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        color = if (prediction.source == PredictionSource.API) Primary else TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(label, fontSize = 12.sp, color = TextSecondary)
    }
}
