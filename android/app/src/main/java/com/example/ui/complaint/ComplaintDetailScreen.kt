package com.example.ui.complaint

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timeline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.Complaint
import com.example.data.model.ComplaintStatus
import com.example.data.model.LpuHostelDirectory
import com.example.data.model.UserRole
import com.example.ui.HostelFixViewModel
import com.example.ui.components.AiInsightCard
import com.example.ui.components.LpuBlockBadge
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusPill
import com.example.ui.components.getCategoryIcon
import com.example.ui.components.safeDialPhone
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintDetailScreen(
    complaintId: String,
    viewModel: HostelFixViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allComplaints by viewModel.allComplaints.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val complaint = allComplaints.firstOrNull { it.id == complaintId }

    val context = LocalContext.current
    var selectedRating by remember(complaint?.id, complaint?.studentFeedback) {
        mutableIntStateOf(complaint?.studentFeedback?.rating ?: 5)
    }
    var feedbackComment by remember(complaint?.id, complaint?.studentFeedback) {
        mutableStateOf(complaint?.studentFeedback?.comment ?: "")
    }
    var showAssignDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var newLogNoteText by remember { mutableStateOf("") }

    val hostelInfo = remember(complaint?.block) {
        complaint?.let { LpuHostelDirectory.findByCode(it.block) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(complaint?.id ?: "Complaint Details", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        if (complaint == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Complaint not found", color = Color(0xFF64748B))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                        .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(complaint.aiAnalysis.category),
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = complaint.aiAnalysis.category.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LpuBlockBadge(blockCode = complaint.block)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Room ${complaint.roomNumber}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                            StatusPill(status = complaint.status)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = complaint.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = complaint.description,
                            fontSize = 14.sp,
                            color = Color(0xFF334155),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${complaint.studentName} (${complaint.studentRoll})",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            PriorityBadge(priority = complaint.aiAnalysis.priority)
                        }
                    }
                }

                // AI Diagnostic Summary Card
                AiInsightCard(
                    aiAnalysis = complaint.aiAnalysis,
                    duplicateInfo = complaint.duplicateInfo
                )

                // LPU Hostel & Duty Warden Information Card
                if (hostelInfo != null) {
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
                                    LpuBlockBadge(blockCode = hostelInfo.code)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = hostelInfo.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEFF6FF)
                                ) {
                                    Text(
                                        text = hostelInfo.type.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Landmark: ${hostelInfo.nearestLandmark}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Duty Warden: ${hostelInfo.wardenName} (${hostelInfo.wardenPhone})",
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Caretaker Contact: ${hostelInfo.caretakerPhone}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = {
                                    safeDialPhone(context, hostelInfo.wardenPhone)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Call ${hostelInfo.code} Duty Warden",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Lifecycle Stepper / Progress Timeline Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Resolution Lifecycle",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val isPending = complaint.status == ComplaintStatus.PENDING
                        val isAssigned = complaint.status == ComplaintStatus.ASSIGNED
                        val isInProgress = complaint.status == ComplaintStatus.IN_PROGRESS
                        val isResolved = complaint.status == ComplaintStatus.RESOLVED
                        val isRejected = complaint.status == ComplaintStatus.REJECTED

                        TimelineStep(
                            title = "Complaint Submitted",
                            subtitle = "AI Engine classified as ${complaint.aiAnalysis.category.displayName} [${complaint.aiAnalysis.priority.label}]",
                            isCompleted = true,
                            isActive = isPending
                        )
                        TimelineStep(
                            title = "Department & Staff Allocation",
                            subtitle = if (complaint.assignedTo != null) "Allocated to ${complaint.assignedTo}" else "Awaiting warden dispatch in ${complaint.aiAnalysis.department}",
                            isCompleted = !isPending && !isRejected,
                            isActive = isAssigned
                        )
                        TimelineStep(
                            title = "Maintenance In Progress",
                            subtitle = if (isInProgress || isResolved) "Technician active on site" else "Pending technician arrival",
                            isCompleted = isInProgress || isResolved,
                            isActive = isInProgress
                        )
                        TimelineStep(
                            title = if (isRejected) "Ticket Withdrawn / Rejected" else "Issue Resolved & Signed Off",
                            subtitle = when {
                                isResolved -> "Maintenance finished and verified"
                                isRejected -> "Ticket was withdrawn or dismissed by warden"
                                else -> "Awaiting maintenance completion"
                            },
                            isCompleted = isResolved || isRejected,
                            isActive = false,
                            isLast = true
                        )
                    }
                }

                // Dedicated Maintenance Activity Log & Audit Trail Card
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
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Maintenance Activity Log",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Official audit trail & field notes",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            // Wardens and Staff can add official log notes
                            if (currentUser?.role == UserRole.WARDEN || currentUser?.role == UserRole.STAFF) {
                                OutlinedButton(
                                    onClick = { showAddNoteDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("add_log_note_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Prominent Field Note Box if present
                        if (!complaint.workNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF3C7).copy(alpha = 0.4f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Official Maintenance Work Note:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = complaint.workNotes,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Chronological Log Entries
                        val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
                        if (complaint.statusHistory.isEmpty()) {
                            Text(
                                text = "No log entries recorded yet.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                complaint.statusHistory.forEach { step ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(8.dp)
                                                .background(
                                                    color = when (step.status) {
                                                        ComplaintStatus.RESOLVED -> Color(0xFF10B981)
                                                        ComplaintStatus.IN_PROGRESS -> PrimaryBlue
                                                        ComplaintStatus.ASSIGNED -> Color(0xFF6366F1)
                                                        ComplaintStatus.REJECTED -> Color(0xFFEF4444)
                                                        else -> Color(0xFFF59E0B)
                                                    },
                                                    shape = CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = step.status.label,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF1E293B)
                                                )
                                                Text(
                                                    text = dateFormat.format(Date(step.timestamp)),
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            }
                                            if (step.note.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = step.note,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Note Dialog
                if (showAddNoteDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showAddNoteDialog = false },
                        title = {
                            Text("Add Maintenance Log Note", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        },
                        text = {
                            Column {
                                Text(
                                    text = "Record technical inspection remarks, spare parts used, or student coordination notes for ticket ${complaint.id}:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = newLogNoteText,
                                    onValueChange = { newLogNoteText = it },
                                    placeholder = { Text("e.g. Replaced capacitor; verified operation with multimeter...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 4,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newLogNoteText.isNotBlank()) {
                                        viewModel.addMaintenanceLogNote(complaint.id, newLogNoteText)
                                        newLogNoteText = ""
                                        showAddNoteDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text("Save Note")
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showAddNoteDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Role-Specific Action Controls
                when (currentUser?.role ?: UserRole.STUDENT) {
                    UserRole.STUDENT -> {
                        // Student Actions: Cancel if pending, or view assigned technician
                        if (complaint.status == ComplaintStatus.PENDING) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Student Actions",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Your request has been routed to ${complaint.aiAnalysis.department}. You can withdraw it if the issue resolved itself.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.cancelStudentComplaint(complaint.id)
                                            onNavigateBack()
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Cancel / Withdraw Request", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    UserRole.WARDEN -> {
                        // Warden Administrative Dispatch Panel
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AssignmentInd,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Warden Supervisory Desk",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Admin Authority",
                                            fontSize = 10.sp,
                                            color = Color(0xFF92400E),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val isClosed = complaint.status == ComplaintStatus.RESOLVED || complaint.status == ComplaintStatus.REJECTED
                                    if (!isClosed) {
                                        Button(
                                            onClick = { showAssignDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                if (complaint.assignedTo != null) "Reassign Staff" else "Assign Staff",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.updateComplaintStatus(
                                                    complaintId = complaint.id,
                                                    newStatus = ComplaintStatus.RESOLVED,
                                                    assignedStaff = complaint.assignedTo
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Sign Off & Resolve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.updateComplaintStatus(
                                                    complaintId = complaint.id,
                                                    newStatus = ComplaintStatus.PENDING,
                                                    assignedStaff = null
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Reopen Ticket", fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            safeDialPhone(context, "+919876543210")
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call Student", fontSize = 11.sp)
                                    }

                                    if (complaint.status != ComplaintStatus.REJECTED && complaint.status != ComplaintStatus.RESOLVED) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.rejectWardenComplaint(complaint.id, "Duplicate or non-permitted request")
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Reject Ticket", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    UserRole.STAFF -> {
                        // Staff Technician Work Order Panel
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBBF7D0))
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Technician Field Order",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF166534)
                                    )
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Duty Technician",
                                            fontSize = 10.sp,
                                            color = Color(0xFF15803D),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

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
                                            Text("Start Repair Work", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (complaint.status != ComplaintStatus.RESOLVED && complaint.status != ComplaintStatus.REJECTED) {
                                        Button(
                                            onClick = { viewModel.completeWorkOrder(complaint.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Mark Work Complete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        safeDialPhone(context, "+919876543210")
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call Student to Unlock Room ${complaint.roomNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (showAssignDialog) {
                    com.example.ui.components.AssignStaffDialog(
                        complaint = complaint,
                        onStaffAssigned = { staffName, staffTrade ->
                            viewModel.assignStaff(complaint.id, staffName, staffTrade)
                            showAssignDialog = false
                        },
                        onDismissRequest = { showAssignDialog = false }
                    )
                }

                // Student Feedback card (if resolved)
                val isStudentUser = currentUser?.role == UserRole.STUDENT
                if (complaint.status == ComplaintStatus.RESOLVED && (isStudentUser || complaint.studentFeedback != null)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBBF7D0))
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SecondaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Student Feedback & Rating",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF166534)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                (1..5).forEach { star ->
                                    if (isStudentUser) {
                                        IconButton(
                                            onClick = { selectedRating = star },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (star <= selectedRating) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "$star Stars",
                                                tint = if (star <= selectedRating) Color(0xFFF59E0B) else Color(0xFFCBD5E1)
                                            )
                                        }
                                    } else {
                                        val existingRating = complaint.studentFeedback?.rating ?: 0
                                        Icon(
                                            imageVector = if (star <= existingRating) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "$star Stars",
                                            tint = if (star <= existingRating) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            if (isStudentUser) {
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = feedbackComment,
                                    onValueChange = { feedbackComment = it },
                                    placeholder = { Text("Comment on repair speed or quality...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        viewModel.submitFeedback(complaint.id, selectedRating, feedbackComment)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (complaint.studentFeedback != null) "Update Rating" else "Submit Rating")
                                }
                            } else if (!complaint.studentFeedback?.comment.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "\"${complaint.studentFeedback?.comment}\"",
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineStep(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        color = when {
                            isCompleted -> Color(0xFF10B981)
                            isActive -> PrimaryBlue
                            else -> Color(0xFFCBD5E1)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(if (isCompleted) Color(0xFF10B981) else Color(0xFFE2E8F0))
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = if (!isLast) 16.dp else 0.dp)) {
            Text(
                text = title,
                fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isActive || isCompleted) Color(0xFF0F172A) else Color(0xFF94A3B8)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
