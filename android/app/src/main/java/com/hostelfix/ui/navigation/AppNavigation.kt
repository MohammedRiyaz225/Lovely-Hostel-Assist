package com.hostelfix.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hostelfix.HostelFixApp
import com.hostelfix.ui.screens.auth.AuthViewModel
import com.hostelfix.ui.screens.auth.AuthViewModelFactory
import com.hostelfix.ui.screens.auth.LoginScreen
import com.hostelfix.ui.screens.auth.RegisterScreen
import com.hostelfix.ui.screens.auth.SplashScreen
import com.hostelfix.ui.screens.complaint.ComplaintDetailScreen
import com.hostelfix.ui.screens.complaint.ComplaintViewModel
import com.hostelfix.ui.screens.complaint.ComplaintViewModelFactory
import com.hostelfix.ui.screens.complaint.CreateComplaintScreen
import com.hostelfix.ui.screens.history.ComplaintHistoryScreen
import com.hostelfix.ui.screens.home.HomeScreen
import com.hostelfix.ui.screens.home.HomeViewModel
import com.hostelfix.ui.screens.home.HomeViewModelFactory
import com.hostelfix.ui.screens.location.LocationViewModel
import com.hostelfix.ui.screens.location.LocationViewModelFactory
import com.hostelfix.ui.screens.location.VerifyLocationScreen
import com.hostelfix.ui.screens.notifications.NotificationsScreen
import com.hostelfix.ui.screens.profile.ProfileScreen

@Composable
fun AppNavigation(
    initialComplaintId: String? = null
) {
    val navController = rememberNavController()
    val app = HostelFixApp.instance
    val context = LocalContext.current

    // ViewModels with factories
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(app.authRepository, app.hostelRepository)
    )
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(app.complaintRepository, app.authRepository, app.tokenManager)
    )
    val locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(app.hostelRepository, context)
    )
    val complaintViewModel: ComplaintViewModel = viewModel(
        factory = ComplaintViewModelFactory(app.complaintRepository, app.hostelRepository, app.tokenManager, context)
    )

    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = if (initialComplaintId != null) {
                    Screen.ComplaintDetail.createRoute(initialComplaintId)
                } else {
                    Screen.Splash.route
                }
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        navController = navController,
                        homeViewModel = homeViewModel
                    )
                }

                composable(Screen.History.route) {
                    ComplaintHistoryScreen(
                        navController = navController,
                        homeViewModel = homeViewModel
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        navController = navController,
                        authRepository = app.authRepository
                    )
                }

                composable(Screen.VerifyLocation.route) {
                    VerifyLocationScreen(
                        navController = navController,
                        locationViewModel = locationViewModel
                    )
                }

                composable(Screen.CreateComplaint.route) {
                    CreateComplaintScreen(
                        navController = navController,
                        complaintViewModel = complaintViewModel
                    )
                }

                composable(
                    route = Screen.ComplaintDetail.route,
                    arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
                    ComplaintDetailScreen(
                        complaintId = complaintId,
                        navController = navController,
                        complaintViewModel = complaintViewModel
                    )
                }

                composable(Screen.Notifications.route) {
                    NotificationsScreen(
                        navController = navController,
                        notificationRepository = app.notificationRepository
                    )
                }
            }
        }
    }
}
