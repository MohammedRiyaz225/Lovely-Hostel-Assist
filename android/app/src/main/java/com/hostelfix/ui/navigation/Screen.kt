package com.hostelfix.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // Main tabs
    object Home : Screen("home")
    object History : Screen("history")
    object Profile : Screen("profile")

    // Modals / flows
    object VerifyLocation : Screen("verify_location")
    object CreateComplaint : Screen("create_complaint")
    object Notifications : Screen("notifications")

    object ComplaintDetail : Screen("complaint_detail/{complaintId}") {
        fun createRoute(complaintId: String) = "complaint_detail/$complaintId"
    }
}
