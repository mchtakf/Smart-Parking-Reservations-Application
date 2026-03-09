package com.example.myapplication.ui.theme.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.theme.navigation.AppBottomNavigationBar
import com.example.myapplication.ui.theme.navigation.NavigationItem
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(
    navController: NavHostController
) {
    val estuLocation = LatLng(39.81528178254205, 30.534890044972165)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(estuLocation, 15f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Şarj İstasyonları", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = estuLocation),
                    title = "ESTÜ Şarj İstasyonu",
                    snippet = "Mühendislik Fakültesi yanı"
                )
            }

            // Bilgi Kartı
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Secondary.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.EvStation, null, tint = Secondary, modifier = Modifier.size(26.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("ESTÜ Şarj İstasyonu", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text("Mühendislik Fakültesi yanı", fontSize = 13.sp, color = TextSecondary)
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        InfoChip(icon = Icons.Default.ElectricBolt, text = "50 kW", color = Warning)
                        InfoChip(icon = Icons.Default.CheckCircle, text = "Aktif", color = Success)
                        InfoChip(icon = Icons.Default.AccessTime, text = "7/24", color = PrimaryLight)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}
