package com.example.myapplication.ui.theme.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val route: String, val title: String, val icon: ImageVector) {
    object Login : NavigationItem("login", "Giriş", Icons.Filled.Person)
    object SignUp : NavigationItem("signup", "Kayıt Ol", Icons.Filled.PersonAdd)
    object Payment : NavigationItem("paymentScreen", "Ödeme", Icons.Filled.CreditCard)
    object Profile : NavigationItem("profile", "Profil", Icons.Filled.Person)
    object NavigationScreen : NavigationItem("navigationScreen", "Harita", Icons.Filled.EvStation)
    object Reservation : NavigationItem("reservation", "Rezervasyon", Icons.Filled.CalendarToday)
}
