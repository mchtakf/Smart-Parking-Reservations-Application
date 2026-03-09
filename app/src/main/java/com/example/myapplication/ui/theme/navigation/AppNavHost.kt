package com.example.myapplication.ui.theme.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.theme.auth.AuthViewModel
import com.example.myapplication.ui.theme.auth.LoginScreen
import com.example.myapplication.ui.theme.auth.SignUpScreen
import com.example.myapplication.ui.theme.main.PaymentScreen
import com.example.myapplication.ui.theme.main.ProfileScreen
import com.example.myapplication.ui.theme.main.RezervasyonEkrani
import com.example.myapplication.ui.theme.main.NavigationScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavigationItem.Login.route // Başlangıç rotası olarak kullanıldı
) {
    // ViewModel'i burada, NavHost'un en üst seviyesinde bir kez oluşturuyoruz.
    // Bu sayede tüm ekranlar aynı ViewModel örneğini paylaşır.
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Login Screen
        composable(NavigationItem.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    // Başarılı girişten sonra geri tuşuna basıldığında login'e dönülmesin diye yığını temizle.
                    navController.navigate(NavigationItem.Reservation.route) {
                        popUpTo(NavigationItem.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(NavigationItem.SignUp.route) }
            )
        }

        // SignUp Screen
        composable(NavigationItem.SignUp.route) {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = {
                    // Başarılı kayıttan sonra geri tuşuna basıldığında login/signup'a dönülmesin.
                    navController.navigate(NavigationItem.Reservation.route) {
                        popUpTo(NavigationItem.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Reservation Screen
        composable(NavigationItem.Reservation.route) {
            // Düzeltme: Hatalı 'onRezervasyonYap' parametresi kaldırıldı.
            // Gerekli olan 'viewModel' parametresi eklendi.
            RezervasyonEkrani(
                navController = navController,
                viewModel = authViewModel
            )
        }

        // Profile Screen
        composable(NavigationItem.Profile.route) {
            // Düzeltme: Gerekli parametreler eklendi.
            ProfileScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Map Screen (Harita)
        composable(NavigationItem.NavigationScreen.route) {
            // Düzeltme: Gerekli 'navController' eklendi.
            NavigationScreen(
                navController = navController
            )
        }

        // Payment Screen
        composable(NavigationItem.Payment.route) {
            // Düzeltme: Gerekli 'navController' eklendi ve rota ismi düzeltildi.
            PaymentScreen(
                navController = navController,
                onPaymentSuccess = {
                    // Ödeme sonrası bir önceki ekrana (Rezervasyon) dön.
                    navController.popBackStack()
                }
            )
        }
    }
}