package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.UserRole
import com.example.ui.HostelFixViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.chat.ChatViewModel
import com.example.ui.complaint.ComplaintDetailScreen
import com.example.ui.complaint.ComplaintListScreen
import com.example.ui.complaint.CreateComplaintScreen
import com.example.ui.home.HomeScreen
import com.example.ui.notification.NotificationScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.staff.StaffDirectoryScreen
import com.example.ui.theme.LavishGold
import com.example.ui.theme.PrimaryBlue

sealed class Screen(val route: String, val title: String) {
    data object Login : Screen("login", "Sign In")
    data object Home : Screen("home", "Home")
    data object ComplaintList : Screen("complaints", "Complaints")
    data object Chat : Screen("ai_chat", "Groq AI")
    data object StaffRoster : Screen("staff_roster", "Staff Team")
    data object CreateComplaint : Screen("create_complaint", "Report")
    data object ComplaintDetail : Screen("complaint_detail/{complaintId}", "Detail") {
        fun createRoute(complaintId: String) = "complaint_detail/$complaintId"
    }
    data object Notifications : Screen("notifications", "Alerts")
    data object Profile : Screen("profile", "Profile")
}

data class NavItemSpec(
    val screen: Screen,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun AppNavigation(
    viewModel: HostelFixViewModel,
    modifier: Modifier = Modifier
) {
    val chatViewModel: ChatViewModel = viewModel()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentUser by viewModel.currentUser.collectAsState()
    val initialStartDestination = remember {
        if (viewModel.currentUser.value != null) Screen.Home.route else Screen.Login.route
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val actionMessage by viewModel.actionMessage.collectAsState()

    LaunchedEffect(actionMessage) {
        actionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    // Role-Adaptive Bottom Navigation Bar
    val bottomNavItems = remember(currentUser?.role) {
        when (currentUser?.role) {
            UserRole.WARDEN -> listOf(
                NavItemSpec(Screen.Home, "Warden Desk", Icons.Default.Home),
                NavItemSpec(Screen.ComplaintList, "Block Triage", Icons.AutoMirrored.Filled.ListAlt),
                NavItemSpec(Screen.StaffRoster, "Staff Team", Icons.Default.Engineering),
                NavItemSpec(Screen.Notifications, "Alerts", Icons.Default.Notifications),
                NavItemSpec(Screen.Profile, "Admin Profile", Icons.Default.Person)
            )
            UserRole.STAFF -> listOf(
                NavItemSpec(Screen.Home, "Work Orders", Icons.Default.Home),
                NavItemSpec(Screen.ComplaintList, "Open Jobs", Icons.AutoMirrored.Filled.ListAlt),
                NavItemSpec(Screen.Chat, "AI Troubleshoot", Icons.Default.AutoAwesome),
                NavItemSpec(Screen.Notifications, "Job Alerts", Icons.Default.Notifications),
                NavItemSpec(Screen.Profile, "Staff Profile", Icons.Default.Person)
            )
            else -> listOf(
                NavItemSpec(Screen.Home, "Home", Icons.Default.Home),
                NavItemSpec(Screen.ComplaintList, "My Room", Icons.AutoMirrored.Filled.ListAlt),
                NavItemSpec(Screen.Chat, "Groq AI", Icons.Default.AutoAwesome),
                NavItemSpec(Screen.Notifications, "Alerts", Icons.Default.Notifications),
                NavItemSpec(Screen.Profile, "Profile", Icons.Default.Person)
            )
        }
    }

    val showBottomBar = currentUser != null && currentRoute in listOf(
        Screen.Home.route,
        Screen.ComplaintList.route,
        Screen.Chat.route,
        Screen.StaffRoster.route,
        Screen.Notifications.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val screen = item.screen
                        val selected = currentRoute == screen.route
                        val isAiTab = screen == Screen.Chat
                        val activeColor = if (isAiTab) LavishGold else PrimaryBlue
                        val indicatorBg = if (isAiTab) Color(0xFFFEF3C7) else Color(0xFFEEF2FF)
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = activeColor,
                                selectedTextColor = activeColor,
                                indicatorColor = indicatorBg,
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialStartDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCreate = { navController.navigate(Screen.CreateComplaint.route) },
                    onNavigateToList = { navController.navigate(Screen.ComplaintList.route) },
                    onNavigateToDetail = { id -> navController.navigate(Screen.ComplaintDetail.createRoute(id)) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                    onLogout = {
                        chatViewModel.clearChat()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Chat.route) {
                ChatScreen(
                    viewModel = chatViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ComplaintList.route) {
                ComplaintListScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { id -> navController.navigate(Screen.ComplaintDetail.createRoute(id)) },
                    onNavigateToCreate = { navController.navigate(Screen.CreateComplaint.route) }
                )
            }

            composable(Screen.StaffRoster.route) {
                StaffDirectoryScreen(
                    viewModel = viewModel,
                    onNavigateToComplaintsForStaff = {
                        navController.navigate(Screen.ComplaintList.route)
                    }
                )
            }

            composable(Screen.CreateComplaint.route) {
                CreateComplaintScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onComplaintCreated = { complaintId ->
                        navController.navigate(Screen.ComplaintDetail.createRoute(complaintId)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(
                route = Screen.ComplaintDetail.route,
                arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
            ) { backStackEntry ->
                val complaintId = backStackEntry.arguments?.getString("complaintId").orEmpty()
                ComplaintDetailScreen(
                    complaintId = complaintId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToComplaint = { complaintId ->
                        navController.navigate(Screen.ComplaintDetail.createRoute(complaintId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        chatViewModel.clearChat()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
