package com.hostelfix.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hostelfix.data.model.ComplaintStatus
import com.hostelfix.ui.navigation.Screen
import com.hostelfix.ui.screens.home.ComplaintItemCard
import com.hostelfix.ui.screens.home.HomeViewModel
import com.hostelfix.ui.theme.BrandOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintHistoryScreen(
    navController: NavController,
    homeViewModel: HomeViewModel
) {
    val state by homeViewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filters = listOf("ALL", "PENDING", "RESOLVED", "CLOSED")

    LaunchedEffect(Unit) {
        homeViewModel.loadData()
    }

    val filteredList = remember(state.recentComplaints, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> state.recentComplaints.filter {
                it.status == ComplaintStatus.SUBMITTED ||
                it.status == ComplaintStatus.AI_ANALYZED ||
                it.status == ComplaintStatus.ASSIGNED ||
                it.status == ComplaintStatus.IN_PROGRESS
            }
            "RESOLVED" -> state.recentComplaints.filter { it.status == ComplaintStatus.RESOLVED }
            "CLOSED" -> state.recentComplaints.filter { it.status == ComplaintStatus.CLOSED }
            else -> state.recentComplaints
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaint History", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { homeViewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandOrange.copy(alpha = 0.2f),
                            selectedLabelColor = BrandOrange
                        )
                    )
                }
            }

            if (state.isLoading && state.recentComplaints.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandOrange)
                }
            } else if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No complaints found under $selectedFilter.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { complaint ->
                        ComplaintItemCard(
                            complaint = complaint,
                            onClick = {
                                navController.navigate(Screen.ComplaintDetail.createRoute(complaint.id))
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
