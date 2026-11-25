package com.example.ui.complaint

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Complaint
import com.example.data.model.ComplaintCategory
import com.example.data.model.ComplaintPriority
import com.example.data.model.ComplaintStatus
import com.example.data.model.LpuHostelDirectory
import com.example.data.model.UserRole
import com.example.ui.HostelFixViewModel
import com.example.ui.components.AssignStaffDialog
import com.example.ui.components.LpuBlockBadge
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusPill
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.LavishGold
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintListScreen(
    viewModel: HostelFixViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val filteredComplaints by viewModel.filteredComplaints.collectAsState()

    var complaintToAssign by remember { mutableStateOf<Complaint?>(null) }

    complaintToAssign?.let { complaint ->
        AssignStaffDialog(
            complaint = complaint,
            onStaffAssigned = { staffName, staffTrade ->
                viewModel.assignStaff(complaint.id, staffName, staffTrade)
                complaintToAssign = null
            },
            onDismissRequest = { complaintToAssign = null }
        )
    }

    val isStudent = currentUser?.role == UserRole.STUDENT
    val isWarden = currentUser?.role == UserRole.WARDEN
    val isStaff = currentUser?.role == UserRole.STAFF

    // Role-tailored tabs
    val tabs = remember(currentUser?.role) {
        when {
            isWarden -> listOf(
                "UNASSIGNED" to "Pending Triage",
                "ALL" to "All Complaints",
                "CRITICAL" to "Critical / High",
                "IN_PROGRESS" to "In Progress",
                "RESOLVED" to "Resolved"
            )
            isStaff -> listOf(
                "ASSIGNED_ME" to "My Tasks",
                "IN_PROGRESS" to "In Progress",
                "RESOLVED" to "Completed",
                "ALL" to "All Open Jobs"
            )
            else -> listOf(
                "ALL" to "All",
                "PENDING" to "Pending",
                "IN_PROGRESS" to "In Progress",
                "RESOLVED" to "Resolved"
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when {
                                isWarden -> "Block Complaints & Triage"
                                isStaff -> "Assigned Work Orders"
                                else -> "Hostel Complaints"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryBlue
                        )
                        Text(
                            text = when {
                                isWarden -> "${currentUser?.name ?: "Hostel Warden"} • Warden Supervisory View"
                                isStaff -> "${currentUser?.name ?: "Technician"} • Maintenance Field View"
                                else -> "${currentUser?.name ?: "Resident Student"} (Room ${currentUser?.roomNumber ?: "204"})"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            // ONLY Students can file new complaints via FAB!
            if (isStudent) {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("create_complaint_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Complaint")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = {
                    Text(
                        if (isStaff) "Search by room, issue, student name..."
                        else "Search by title, description, block, ID..."
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B))
                },
                trailingIcon = {
                    if (filterState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("search_complaints_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            // For Students: Scope Toggle (My Room vs Hostel Block Feed)
            if (isStudent) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isMyRoom = filterState.scopeTab == "MY_ROOM"
                    val isBlockFeed = filterState.scopeTab == "BLOCK_FEED"
                    val isAllCampus = filterState.scopeTab == "ALL"

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.updateScopeTab("MY_ROOM") },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isMyRoom) PrimaryBlue else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "My Room (${currentUser?.roomNumber ?: "204"})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMyRoom) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.updateScopeTab("BLOCK_FEED") },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isBlockFeed) PrimaryBlue else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "${currentUser?.hostelBlock ?: "BH-4"} Feed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBlockFeed) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.updateScopeTab("ALL") },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isAllCampus) PrimaryBlue else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "Campus",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllCampus) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Role Status Tabs
            val selectedTabIndex = tabs.indexOfFirst {
                if (isStaff && it.first == "ASSIGNED_ME") filterState.scopeTab == "ASSIGNED_ME"
                else it.first == filterState.statusTab
            }.coerceAtLeast(0)

            SecondaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, (key, title) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            if (isStaff && key == "ASSIGNED_ME") {
                                viewModel.updateScopeTab("ASSIGNED_ME")
                                viewModel.updateStatusTab("ALL")
                            } else {
                                if (isStaff) viewModel.updateScopeTab("ALL")
                                viewModel.updateStatusTab(key)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // LPU Hostel Block Filter Row (Visible to Warden & Students)
            if (!isStaff) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 2.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        CategoryFilterChip(
                            label = "All Hostels",
                            isSelected = filterState.selectedBlock == null,
                            onClick = { viewModel.updateBlockFilter(null) }
                        )
                    }
                    items(LpuHostelDirectory.allCodes) { blockCode ->
                        val isSelected = filterState.selectedBlock.equals(blockCode, ignoreCase = true)
                        Surface(
                            modifier = Modifier.clickable {
                                viewModel.updateBlockFilter(if (isSelected) null else blockCode)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) PrimaryBlue else Color(0xFFFBF8FE),
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = blockCode,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryFilterChip(
                        label = "All Categories",
                        isSelected = filterState.selectedCategory == null,
                        onClick = { viewModel.updateCategoryFilter(null) }
                    )
                }
                items(ComplaintCategory.entries) { cat ->
                    CategoryFilterChip(
                        label = cat.displayName,
                        isSelected = filterState.selectedCategory == cat,
                        onClick = {
                            viewModel.updateCategoryFilter(if (filterState.selectedCategory == cat) null else cat)
                        }
                    )
                }
            }

            // Results count & reset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${filteredComplaints.size} complaints",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    if (filterState.selectedBlock != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        LpuBlockBadge(blockCode = filterState.selectedBlock!!)
                    }
                }
                if (filterState.selectedCategory != null || filterState.selectedPriority != null || filterState.selectedBlock != null || filterState.searchQuery.isNotEmpty()) {
                    Text(
                        text = "Reset Filters",
                        fontSize = 12.sp,
                        color = LavishGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            viewModel.updateCategoryFilter(null)
                            viewModel.updatePriorityFilter(null)
                            viewModel.updateBlockFilter(null)
                            viewModel.updateSearchQuery("")
                            if (isStudent) viewModel.updateScopeTab("MY_ROOM")
                        }
                    )
                }
            }

            // List of complaints
            if (filteredComplaints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching complaints",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (isStudent) "No complaints filed by your room matching these filters." else "Try adjusting your filters or search keywords.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredComplaints, key = { it.id }) { complaint ->
                        RoleAwareComplaintCard(
                            complaint = complaint,
                            userRole = currentUser?.role ?: UserRole.STUDENT,
                            onClick = { onNavigateToDetail(complaint.id) },
                            onAssignClick = { complaintToAssign = complaint },
                            onStartWork = { viewModel.startWorkOrder(complaint.id) },
                            onResolveWork = { viewModel.completeWorkOrder(complaint.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PrimaryBlue else Color(0xFFFBF8FE),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun RoleAwareComplaintCard(
    complaint: Complaint,
    userRole: UserRole,
    onClick: () -> Unit,
    onAssignClick: () -> Unit,
    onStartWork: () -> Unit,
    onResolveWork: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("complaint_row_${complaint.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getCategoryIcon(complaint.aiAnalysis.category),
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = complaint.id,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${complaint.aiAnalysis.category.displayName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                }
                StatusPill(status = complaint.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = complaint.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = complaint.description,
                fontSize = 12.sp,
                color = Color(0xFF475569),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LpuBlockBadge(blockCode = complaint.block)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Room ${complaint.roomNumber}",
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium
                        )
                        if (complaint.assignedTo != null) {
                            Text(
                                text = "Staff: ${complaint.assignedTo}",
                                fontSize = 11.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                PriorityBadge(priority = complaint.aiAnalysis.priority)
            }

            // Role-Specific Action Buttons embedded right into the card
            if (userRole == UserRole.WARDEN &&
                complaint.status != ComplaintStatus.RESOLVED &&
                complaint.status != ComplaintStatus.REJECTED &&
                (complaint.status == ComplaintStatus.PENDING || complaint.assignedTo.isNullOrBlank())
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAssignClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assign Staff", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (userRole == UserRole.STAFF &&
                complaint.status != ComplaintStatus.RESOLVED &&
                complaint.status != ComplaintStatus.REJECTED
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (complaint.status == ComplaintStatus.ASSIGNED || complaint.status == ComplaintStatus.PENDING) {
                        Button(
                            onClick = onStartWork,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Repair", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (complaint.status == ComplaintStatus.IN_PROGRESS) {
                        Button(
                            onClick = onResolveWork,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Resolved", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
