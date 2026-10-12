package com.hostelfix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hostelfix.ui.navigation.AppNavigation
import com.hostelfix.ui.theme.HostelFixTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val complaintIdFromPush = intent.getStringExtra("complaintId")

        setContent {
            HostelFixTheme {
                AppNavigation(initialComplaintId = complaintIdFromPush)
            }
        }
    }
}
