package com.example.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Complaint
import com.example.data.model.ComplaintPriority
import com.example.data.model.ComplaintStatus
import com.example.data.model.LpuHostelDirectory
import com.example.data.model.MaintenanceStaffDirectory
import com.example.data.model.UserRole
import com.example.ui.HostelFixViewModel
import com.example.ui.components.AssignStaffDialog
import com.example.ui.components.LpuBlockBadge
import com.example.ui.components.LpuBlockPickerSheet
import com.example.ui.components.LpuEmergencyDirectoryDialog
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusPill
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.LavishChampagneBorder
import com.example.ui.theme.LavishGold
import com.example.ui.theme.LavishGoldWarm
import com.example.ui.theme.LavishRoyalGradient
import com.example.ui.theme.OutlineGold
import com.example.ui.theme.OutlinePurple
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.PriorityCriticalColor
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.TertiaryIndigo

import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HostelFixViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val allComplaints by viewModel.allComplaints.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    var showBlockPickerSheet by remember { mutableStateOf(false) }
    var showEmergencyDirectoryDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var complaintToAssign by remember { mutableStateOf<Complaint?>(null) }

    val userHostel = remember(currentUser?.hostelBlock) {
        LpuHostelDirectory.findByCode(currentUser?.hostelBlock ?: "BH-4")
    }

    if (showBlockPickerSheet) {
        LpuBlockPickerSheet(
            selectedBlockCode = currentUser?.hostelBlock ?: "BH-4",
            onBlockSelected = { selectedHostel ->
                viewModel.updateUserHostelBlock(selectedHostel.code, currentUser?.roomNumber ?: "204")
                showBlockPickerSheet = false
            },
            onDismissRequest = { showBlockPickerSheet = false }
        )
    }

    if (showEmergencyDirectoryDialog) {
        LpuEmergencyDirectoryDialog(
            onDismissRequest = { showEmergencyDirectoryDialog = false }
        )
    }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when {
                                    isWarden -> "HostelFix • Warden Desk"
                                    isStaff -> "HostelFix • Maintenance"
                                    else -> "HostelFix"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = LavishGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (isStudent) showBlockPickerSheet = true
                            }
                        ) {
                            LpuBlockBadge(blockCode = currentUser?.hostelBlock ?: "BH-4")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isWarden -> "Campus Oversight & Triage"
                                    isStaff -> "Field Operations (${currentUser?.departmentOrTrade ?: "Staff"})"
                                    else -> "Room ${currentUser?.roomNumber ?: "204"} ▾"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.testTag("home_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log Out",
                            tint = Color(0xFF64748B)
                        )
                    }
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(
                            onClick = onNavigateToNotifications,
                            modifier = Modifier.testTag("notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = PrimaryBlue
                            )
                        }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                                    .size(16.dp)
                                    .background(PriorityCriticalColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            // ONLY Students have the "Report Issue" FAB! Wardens manage/dispatch, Staff resolve.
            if (isStudent) {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("create_complaint_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Report Complaint", tint = LavishGoldWarm)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Report Issue", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Session Identity Badge
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (currentUser?.role) {
                            UserRole.STUDENT -> Color(0xFFEFF6FF)
                            UserRole.WARDEN -> Color(0xFFFEF3C7).copy(alpha = 0.5f)
                            UserRole.STAFF -> Color(0xFFECFDF5)
                            else -> Color(0xFFFAF5FF)
                        }
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            when (currentUser?.role) {
                                UserRole.STUDENT -> Color(0xFFBFDBFE)
                                UserRole.WARDEN -> Color(0xFFFDE68A)
                                UserRole.STAFF -> Color(0xFFA7F3D0)
                                else -> Color(0xFFE9D5FF)
                            }
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (currentUser?.role) {
                                    UserRole.STUDENT -> Icons.Default.School
                                    UserRole.WARDEN -> Icons.Default.Security
                                    UserRole.STAFF -> Icons.Default.Engineering
                                    else -> Icons.Default.Person
                                },
                                contentDescription = null,
                                tint = when (currentUser?.role) {
                                    UserRole.STUDENT -> PrimaryBlue
                                    UserRole.WARDEN -> LavishGold
                                    UserRole.STAFF -> SecondaryTeal
                                    else -> PrimaryBlue
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentUser?.name ?: "User",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                )
                                val idLabel = when (currentUser?.role) {
                                    UserRole.STUDENT -> "Reg No: ${currentUser?.rollNumber ?: ""}"
                                    UserRole.WARDEN -> "Employee ID: ${currentUser?.rollNumber ?: ""}"
                                    UserRole.STAFF -> "Staff ID: ${currentUser?.rollNumber ?: ""}"
                                    else -> "ID: ${currentUser?.rollNumber ?: ""}"
                                }
                                Text(
                                    text = "${currentUser?.role?.displayName ?: "Student"} Account • $idLabel",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Surface(
                            color = when (currentUser?.role) {
                                UserRole.STUDENT -> Color(0xFFDBEAFE)
                                UserRole.WARDEN -> Color(0xFFFEF3C7)
                                UserRole.STAFF -> Color(0xFFDCFCE7)
                                else -> Color(0xFFF1F5F9)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Authenticated",
                                fontSize = 10.sp,
                                color = when (currentUser?.role) {
                                    UserRole.STUDENT -> PrimaryBlue
                                    UserRole.WARDEN -> Color(0xFF92400E)
                                    UserRole.STAFF -> Color(0xFF166534)
                                    else -> Color(0xFF475569)
                                },
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // ROLE-SPECIFIC CONTENT
            // =========================================================================

            if (isStudent) {
                // --------------------- STUDENT JOURNEY ---------------------
                // Lavish AI Feature Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(LavishRoyalGradient)
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFFEF3C7).copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = LavishGoldWarm,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Smart Student Resident Portal",
                                            color = Color(0xFFFEF3C7),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Have an Issue in Room ${currentUser?.roomNumber ?: "204"}?",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Report plumbing, electrical, wifi, or furniture issues. AI automatically identifies urgency and alerts maintenance staff instantly.",
                                color = Color(0xFFE0E7FF),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = onNavigateToCreate,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LavishGold,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Report New Issue", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = onNavigateToChat,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE)),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Ask Groq AI", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Student Stats Summary
                val studentRoll = currentUser?.rollNumber.orEmpty()
                val studentRoom = currentUser?.roomNumber.orEmpty()
                val studentBlock = currentUser?.hostelBlock.orEmpty()
                val myComplaints = allComplaints.filter {
                    (studentRoll.isNotBlank() && it.studentRoll.equals(studentRoll, ignoreCase = true)) ||
                        (studentBlock.isNotBlank() && studentRoom.isNotBlank() &&
                            it.block.equals(studentBlock, ignoreCase = true) &&
                            it.roomNumber.equals(studentRoom, ignoreCase = true))
                }
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "My Room Complaints Tracker",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "View All (${myComplaints.size})",
                                fontSize = 13.sp,
                                color = PrimaryBlueLight,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    viewModel.updateBlockFilter(null)
                                    viewModel.updateStatusTab("ALL")
                                    viewModel.updateScopeTab("MY_ROOM")
                                    onNavigateToList()
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "My Active",
                                count = myComplaints.count { it.status != ComplaintStatus.RESOLVED && it.status != ComplaintStatus.REJECTED }.toString(),
                                icon = Icons.Default.PendingActions,
                                bgColor = Color(0xFFFEF3C7),
                                textColor = LavishGold,
                                borderColor = LavishChampagneBorder,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "In Progress",
                                count = myComplaints.count { it.status == ComplaintStatus.IN_PROGRESS || it.status == ComplaintStatus.ASSIGNED }.toString(),
                                icon = Icons.Default.AutoAwesome,
                                bgColor = Color(0xFFF3E8FF),
                                textColor = TertiaryIndigo,
                                borderColor = OutlinePurple,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Resolved",
                                count = myComplaints.count { it.status == ComplaintStatus.RESOLVED }.toString(),
                                icon = Icons.Default.CheckCircle,
                                bgColor = Color(0xFFECFDF5),
                                textColor = SecondaryTeal,
                                borderColor = Color(0xFFA7F3D0),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // My Active Complaints List Section
                val myActiveList = myComplaints.filter {
                    it.status != ComplaintStatus.RESOLVED && it.status != ComplaintStatus.REJECTED
                }
                item {
                    Text(
                        text = "My Active Maintenance Requests",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                if (myActiveList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("No pending issues in Room ${currentUser?.roomNumber ?: "204"}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Everything is running smoothly! Tap below to report any maintenance problem.", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                } else {
                    items(myActiveList) { complaint ->
                        ComplaintHomeItem(
                            complaint = complaint,
                            onClick = { onNavigateToDetail(complaint.id) }
                        )
                    }
                }

            } else if (isWarden) {
                // --------------------- WARDEN / ADMIN JOURNEY ---------------------
                // Warden Critical Urgent Alert Card (if critical complaints exist)
                val criticals = allComplaints.filter {
                    it.aiAnalysis.priority == ComplaintPriority.CRITICAL &&
                        it.status != ComplaintStatus.RESOLVED &&
                        it.status != ComplaintStatus.REJECTED
                }
                if (criticals.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCA5A5))
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Urgent Escalation (${criticals.size} Critical)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF991B1B)
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFFFEE2E2),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Immediate Action",
                                            fontSize = 10.sp,
                                            color = Color(0xFFB91C1C),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${criticals.first().title} in ${criticals.first().block} (Room ${criticals.first().roomNumber})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7F1D1D)
                                )
                                Text(
                                    text = criticals.first().aiAnalysis.summary,
                                    fontSize = 11.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onNavigateToDetail(criticals.first().id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Review & Dispatch Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Warden Supervisory KPIs
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Block Oversight & Operations",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Triage Desk",
                                fontSize = 12.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    viewModel.updateScopeTab("ALL")
                                    onNavigateToList()
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Unassigned",
                                count = stats.unassignedCount.toString(),
                                icon = Icons.Default.PendingActions,
                                bgColor = Color(0xFFFEF3C7),
                                textColor = LavishGold,
                                borderColor = LavishChampagneBorder,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.updateScopeTab("ALL")
                                        viewModel.updateStatusTab("UNASSIGNED")
                                        onNavigateToList()
                                    }
                            )
                            StatCard(
                                title = "In Progress",
                                count = stats.inProgress.toString(),
                                icon = Icons.Default.Engineering,
                                bgColor = Color(0xFFEEF2FF),
                                textColor = PrimaryBlue,
                                borderColor = Color(0xFFC7D2FE),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.updateScopeTab("ALL")
                                        viewModel.updateStatusTab("IN_PROGRESS")
                                        onNavigateToList()
                                    }
                            )
                            StatCard(
                                title = "Resolved",
                                count = stats.resolved.toString(),
                                icon = Icons.Default.CheckCircle,
                                bgColor = Color(0xFFECFDF5),
                                textColor = SecondaryTeal,
                                borderColor = Color(0xFFA7F3D0),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.updateScopeTab("ALL")
                                        viewModel.updateStatusTab("RESOLVED")
                                        onNavigateToList()
                                    }
                            )
                            StatCard(
                                title = "Critical",
                                count = stats.highPriority.toString(),
                                icon = Icons.Default.Warning,
                                bgColor = Color(0xFFFFF1F2),
                                textColor = Color(0xFFE11D48),
                                borderColor = Color(0xFFFECDD3),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.updateScopeTab("ALL")
                                        viewModel.updateStatusTab("CRITICAL")
                                        onNavigateToList()
                                    }
                            )
                        }
                    }
                }

                // Unassigned Complaints Queue for Warden Triage
                val unassignedList = allComplaints.filter { it.assignedTo.isNullOrBlank() && it.status == ComplaintStatus.PENDING }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pending Staff Assignment Queue",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${unassignedList.size} tickets needing staff",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LavishGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (unassignedList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryTeal)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("All pending complaints have been assigned to technicians!", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    items(unassignedList) { complaint ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LpuBlockBadge(blockCode = complaint.block)
                                    PriorityBadge(priority = complaint.aiAnalysis.priority)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(complaint.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "Room ${complaint.roomNumber} • Student: ${complaint.studentName} (${complaint.studentRoll})",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { complaintToAssign = complaint },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Assign Staff", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { onNavigateToDetail(complaint.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text("Review Details", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Duplicate Complaints Radar
                val duplicateComplaints = allComplaints.filter { it.duplicateInfo?.isDuplicate == true }
                if (duplicateComplaints.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFDE68A))
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = LavishGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI Duplicate Complaints Radar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${duplicateComplaints.size} complaints flagged with identical descriptions in the same block/wing. You can merge tickets to optimize staff visits.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }

            } else if (isStaff) {
                // --------------------- MAINTENANCE STAFF JOURNEY ---------------------
                // Staff Field Work Orders Banner
                val staffName = currentUser?.name.orEmpty()
                val staffId = currentUser?.rollNumber.orEmpty()
                val staffTrade = currentUser?.assignedTrade
                val assignedToStaff = allComplaints.filter {
                    it.status != ComplaintStatus.REJECTED && (
                        (staffName.isNotBlank() && it.assignedTo?.contains(staffName, ignoreCase = true) == true) ||
                        (staffId.isNotBlank() && it.assignedTo?.contains(staffId, ignoreCase = true) == true) ||
                        (staffTrade != null && it.aiAnalysis.category == staffTrade)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFC7D2FE))
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(PrimaryBlue, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Engineering, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Technician Field Duty",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = PrimaryBlue
                                        )
                                        Text(
                                            text = currentUser?.departmentOrTrade ?: "Senior Maintenance Specialist",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "On Duty",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "You have ${assignedToStaff.count { it.status != ComplaintStatus.RESOLVED && it.status != ComplaintStatus.REJECTED }} active work orders assigned across LPU campus hostels.",
                                fontSize = 12.sp,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }

                // Work Order Cards for Staff
                item {
                    Text(
                        text = "My Assigned Work Orders",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                if (assignedToStaff.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("No pending work orders assigned to you right now.", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Check the 'Open Jobs' tab to view unassigned complaints in your department.", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                } else {
                    items(assignedToStaff) { complaint ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LpuBlockBadge(blockCode = complaint.block)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Room ${complaint.roomNumber}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    StatusPill(status = complaint.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = complaint.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )

                                Text(
                                    text = complaint.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569),
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = LavishGold, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Est. Time: ${complaint.aiAnalysis.estimatedResolutionHours}h • Urgency: ${complaint.aiAnalysis.priority.label}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Technician Quick Action Controls
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (complaint.status == ComplaintStatus.ASSIGNED || complaint.status == ComplaintStatus.PENDING) {
                                        Button(
                                            onClick = { viewModel.startWorkOrder(complaint.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Start Work", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (complaint.status == ComplaintStatus.IN_PROGRESS) {
                                        Button(
                                            onClick = { viewModel.completeWorkOrder(complaint.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Mark Resolved", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { onNavigateToDetail(complaint.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Full Details", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // SHARED SECTIONS (Emergency helpline, LPU 18 Hostels directory carousel)
            // =========================================================================

            // Emergency helpline quick card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFFFFE4E6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${userHostel?.name ?: currentUser?.hostelBlock ?: "Hostel"} Warden Desk",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF9F1239)
                                )
                                Text(
                                    text = "${userHostel?.wardenName ?: "Duty Warden"}: ${userHostel?.wardenPhone ?: "+91 1824 517200"}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFBE123C),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Uni-Hospital: +91 1824 517000 (24x7)",
                                fontSize = 11.sp,
                                color = Color(0xFF9F1239)
                            )
                            Surface(
                                modifier = Modifier.clickable { showEmergencyDirectoryDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFE4E6)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Emergency,
                                        contentDescription = null,
                                        tint = Color(0xFFE11D48),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "All Campus Helplines",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFBE123C)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // LPU Hostel Blocks Hub Carousel (for easy block switching and exploration)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All LPU Hostel Blocks",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "18 Blocks Directory",
                            fontSize = 12.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showBlockPickerSheet = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        items(LpuHostelDirectory.allHostels) { hostel ->
                            val blockComplaints = allComplaints.count { it.block.equals(hostel.code, ignoreCase = true) }
                            Card(
                                modifier = Modifier
                                    .width(190.dp)
                                    .clickable {
                                        viewModel.updateScopeTab("ALL")
                                        viewModel.updateStatusTab("ALL")
                                        viewModel.updateCategoryFilter(null)
                                        viewModel.updatePriorityFilter(null)
                                        viewModel.updateSearchQuery("")
                                        viewModel.updateBlockFilter(hostel.code)
                                        onNavigateToList()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LpuBlockBadge(blockCode = hostel.code)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (blockComplaints > 0) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
                                        ) {
                                            Text(
                                                text = "$blockComplaints logged",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (blockComplaints > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = hostel.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "${hostel.totalFloors} Floors • ${hostel.wings.joinToString(", ")}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Sign Out Confirmation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out from the ${currentUser?.role?.displayName ?: "User"} portal?",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    }
                ) {
                    Text("Log Out", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}

@Composable
fun RoleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryBlue else Color.White,
        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF475569),
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    bgColor: Color,
    textColor: Color,
    borderColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (borderColor != Color.Transparent) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ) else null
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = textColor
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ComplaintHomeItem(
    complaint: Complaint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("complaint_item_${complaint.id}"),
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(complaint.aiAnalysis.category),
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = complaint.id,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = complaint.aiAnalysis.category.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
                StatusPill(status = complaint.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = complaint.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
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
                Text(
                    text = "${complaint.block} • Room ${complaint.roomNumber}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                PriorityBadge(priority = complaint.aiAnalysis.priority)
            }
        }
    }
}
