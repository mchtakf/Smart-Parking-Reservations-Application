package com.example.myapplication.ui.theme.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.ui.theme.*
import com.example.myapplication.ui.theme.auth.AuthViewModel
import com.example.myapplication.ui.theme.navigation.NavigationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val userProfile by authViewModel.userProfileData.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var vehicleName by remember { mutableStateOf("") }
    var vehiclePlate by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.name; email = it.email; phone = it.phone
            vehicleName = it.vehicleName; vehiclePlate = it.vehiclePlate; vehicleModel = it.vehicleModel
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate(NavigationItem.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Outlined.Logout, "Çıkış", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (userProfile == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                // Profil Başlık Alanı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(GradientStart, PrimaryLight))
                        )
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(email, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    // Kişisel Bilgiler
                    ProfileSectionCard(title = "Kişisel Bilgiler", icon = Icons.Default.Person) {
                        ProfileField(value = name, onValueChange = { name = it }, label = "Ad Soyad", icon = Icons.Default.Person)
                        ProfileField(value = email, onValueChange = {}, label = "E-posta", icon = Icons.Default.Email, enabled = false)
                        ProfileField(value = phone, onValueChange = { phone = it }, label = "Telefon", icon = Icons.Default.Phone)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Araç Bilgileri
                    ProfileSectionCard(title = "Araç Bilgileri", icon = Icons.Default.DirectionsCar) {
                        ProfileField(value = vehicleName, onValueChange = { vehicleName = it }, label = "Araç İsmi", icon = Icons.Default.DirectionsCar)
                        ProfileField(value = vehiclePlate, onValueChange = { vehiclePlate = it }, label = "Plaka", icon = Icons.Default.ConfirmationNumber)
                        ProfileField(value = vehicleModel, onValueChange = { vehicleModel = it }, label = "Model", icon = Icons.Default.Settings)
                    }

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = {
                            authViewModel.updateUserProfile(name, phone, vehicleName, vehiclePlate, vehicleModel)
                            Toast.makeText(context, "Profil güncellendi!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Bilgileri Güncelle", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Primary)
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = if (enabled) PrimaryLight else TextSecondary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        enabled = enabled,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary, unfocusedBorderColor = Color(0xFFE0E0E0),
            disabledBorderColor = Color(0xFFEEEEEE), disabledTextColor = TextSecondary,
            cursorColor = Primary
        )
    )
}
