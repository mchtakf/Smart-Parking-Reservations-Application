package com.example.myapplication.ui.theme.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel = viewModel(),
    onSignUpSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var vehicleName by remember { mutableStateOf("") }
    var vehiclePlate by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }

    val signUpState by authViewModel.signUpState.collectAsState(initial = AuthState.Idle)

    LaunchedEffect(signUpState) {
        if (signUpState is AuthState.Success) onSignUpSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd, Color(0xFF42A5F5))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = Color.White
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("Hesap Oluştur", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Bilgilerinizi girin", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)

            Spacer(Modifier.height(24.dp))

            // Kişisel Bilgiler Kartı
            SectionCard(title = "Kişisel Bilgiler") {
                ModernTextField(value = name, onValueChange = { name = it }, label = "Ad Soyad", icon = Icons.Default.Person)
                ModernTextField(value = phone, onValueChange = { phone = it }, label = "Telefon", icon = Icons.Default.Phone)
                ModernTextField(value = email, onValueChange = { email = it }, label = "E-posta", icon = Icons.Default.Email)
                ModernTextField(value = password, onValueChange = { password = it }, label = "Şifre", icon = Icons.Default.Lock, isPassword = true)
                ModernTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Şifre Tekrar", icon = Icons.Default.Lock, isPassword = true)
            }

            Spacer(Modifier.height(16.dp))

            // Araç Bilgileri Kartı
            SectionCard(title = "Araç Bilgileri") {
                ModernTextField(value = vehicleName, onValueChange = { vehicleName = it }, label = "Araç İsmi", icon = Icons.Default.DirectionsCar)
                ModernTextField(value = vehiclePlate, onValueChange = { vehiclePlate = it }, label = "Plaka", icon = Icons.Default.ConfirmationNumber)
                ModernTextField(value = vehicleModel, onValueChange = { vehicleModel = it }, label = "Araç Modeli", icon = Icons.Default.Settings)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    authViewModel.signUp(email, password, name, phone, vehicleName, vehiclePlate, vehicleModel)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (signUpState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary, strokeWidth = 2.dp)
                } else {
                    Text("Kayıt Ol", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
                }
            }

            if (signUpState is AuthState.Error) {
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)), shape = RoundedCornerShape(10.dp)) {
                    Text((signUpState as AuthState.Error).message, color = Error, fontSize = 13.sp, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Zaten hesabınız var mı?", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            TextButton(onClick = onNavigateToLogin) {
                Text("Giriş Yap", color = Secondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Primary)
            HorizontalDivider(color = Color(0xFFEEEEEE))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = PrimaryLight) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedLabelColor = Primary,
            cursorColor = Primary
        ),
        singleLine = true
    )
}
