package com.example.myapplication.ui.theme.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavHostController,
    onPaymentSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ödeme", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
            // Kart Görseli
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .shadow(16.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(GradientStart, PrimaryLight, Color(0xFF42A5F5))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CreditCard, null, tint = Color.White, modifier = Modifier.size(40.dp))
                            Text("VISA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = if (cardNumber.isNotEmpty()) cardNumber.chunked(4).joinToString("  ") else "**** **** **** ****",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Kart Sahibi", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                Text(name.ifEmpty { "AD SOYAD" }, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Son Kullanma", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                Text(expiryDate.ifEmpty { "AA/YY" }, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Form Kartı
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Kart Bilgileri", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Kart Üzerindeki İsim") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = PrimaryLight) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Color(0xFFE0E0E0), cursorColor = Primary)
                    )

                    OutlinedTextField(
                        value = cardNumber, onValueChange = { if (it.length <= 16) cardNumber = it },
                        label = { Text("Kart Numarası") },
                        leadingIcon = { Icon(Icons.Outlined.CreditCard, null, tint = PrimaryLight) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Color(0xFFE0E0E0), cursorColor = Primary)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = expiryDate, onValueChange = { if (it.length <= 5) expiryDate = it },
                            label = { Text("AA/YY") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = PrimaryLight) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Color(0xFFE0E0E0), cursorColor = Primary)
                        )
                        OutlinedTextField(
                            value = cvv, onValueChange = { if (it.length <= 3) cvv = it },
                            label = { Text("CVV") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryLight) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Color(0xFFE0E0E0), cursorColor = Primary)
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onPaymentSuccess,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ödemeyi Onayla", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
