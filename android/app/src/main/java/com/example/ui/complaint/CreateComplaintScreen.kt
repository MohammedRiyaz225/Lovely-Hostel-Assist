package com.example.ui.complaint

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HostelFixViewModel
import com.example.data.model.LpuHostelDirectory
import com.example.ui.components.LpuBlockBadge
import com.example.ui.components.LpuBlockPickerSheet
import com.example.ui.components.PriorityBadge
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.LavishGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PriorityCriticalColor
import com.example.ui.theme.PriorityHighColor
import com.example.ui.theme.PriorityLowColor
import com.example.ui.theme.SecondaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateComplaintScreen(
    viewModel: HostelFixViewModel,
    onNavigateBack: () -> Unit,
    onComplaintCreated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val liveAiState by viewModel.liveAiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var block by remember { mutableStateOf(currentUser?.hostelBlock ?: "BH-4") }
    var roomNumber by remember { mutableStateOf(currentUser?.roomNumber ?: "204") }
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showBlockPicker by remember { mutableStateOf(false) }

    val currentHostel = remember(block) {
        LpuHostelDirectory.findByCode(block) ?: LpuHostelDirectory.findByCode("BH-4")
    }

    // Modern Photo Picker (Zero-permission Android Photo Picker compliant with Play Policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri.toString()
        }
    }

    // Trigger live AI analysis on text updates
    LaunchedEffect(title, description, block) {
        viewModel.onComplaintInputChanged(title, description, block)
    }

    val quickTemplates = listOf(
        Triple("Water leakage in washroom tap", "Water tap in BH-4 2nd floor washroom has been leaking for three days continuously.", "BH-4"),
        Triple("Ceiling fan regulator sparking", "The ceiling fan regulator in BH-4 room 204 sparks when turned to speed 3.", "BH-4"),
        Triple("Wi-Fi keeps dropping connection", "Unable to connect to LPU hostel Wi-Fi network in BH-1 Wing C.", "BH-1"),
        Triple("Geyser MCB tripping in washroom", "Turning on water geyser in GH-2 3rd floor washroom instantly trips breaker.", "GH-2"),
        Triple("Kitchen sink clogged in apartment", "Wastewater is backing up from kitchen sink in Uni-Living Tower 2.", "Uni-Living"),
        Triple("Study table drawer lock broken", "Wooden drawer lock mechanism came off in BH-7 room 118.", "BH-7")
    )

    if (showBlockPicker) {
        LpuBlockPickerSheet(
            selectedBlockCode = block,
            onBlockSelected = { selectedHostel ->
                block = selectedHostel.code
                showBlockPicker = false
            },
            onDismissRequest = { showBlockPicker = false }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Report Complaint", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick viva sample chips
            Column {
                Text(
                    text = "Quick Demo Templates (LPU Hostel Scenarios)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(quickTemplates) { (t, d, b) ->
                        Surface(
                            modifier = Modifier.clickable {
                                title = t
                                description = d
                                block = b
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEFF6FF),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LpuBlockBadge(blockCode = b)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = t,
                                    fontSize = 11.sp,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Location card with LPU Block Picker
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LPU Hostel Block & Location",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        TextButton(
                            onClick = { showBlockPicker = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Change Block (All 18+)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selected LPU Block Display Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBlockPicker = true },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LpuBlockBadge(blockCode = block)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentHostel?.name ?: block,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = currentHostel?.nearestLandmark ?: "Lovely Professional University",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "Warden: ${currentHostel?.wardenName ?: "Hostel Cell"} (${currentHostel?.wardenPhone ?: "+91 1824 517200"})",
                                        fontSize = 10.sp,
                                        color = Color(0xFF0369A1)
                                    )
                                }
                            }
                        }
                    }

                    // Quick Wings Chips if available for this block
                    if (currentHostel != null && currentHostel.wings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Wing: ",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(currentHostel.wings) { wing ->
                                    val isWingSelected = roomNumber.contains(wing, ignoreCase = true)
                                    Surface(
                                        modifier = Modifier.clickable {
                                            val baseRoom = currentHostel.wings.fold(roomNumber) { acc, w ->
                                                acc.replace(w, "", ignoreCase = true)
                                            }.replace("  ", " ").trim()
                                            roomNumber = if (isWingSelected) {
                                                baseRoom
                                            } else {
                                                listOf(wing, baseRoom.ifBlank { "Room 204" }).joinToString(" ")
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isWingSelected) PrimaryBlue else Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            text = wing,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isWingSelected) Color.White else Color(0xFF334155),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = block,
                            onValueChange = { block = it },
                            label = { Text("Block Code") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("block_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = roomNumber,
                            onValueChange = { roomNumber = it },
                            label = { Text("Room / Area (e.g. 204)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("room_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Issue Title (e.g., Water leakage in washroom)") },
                placeholder = { Text("Brief summary of the issue") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("complaint_title_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Description") },
                placeholder = { Text("Describe the problem clearly (duration, specific spot, urgency)...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("complaint_desc_input"),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            // Image attachment option
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (selectedPhotoUri != null) "Photo attached" else "Attach Photo (Optional)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (selectedPhotoUri != null) "Ready for inspection" else "Take or pick photo proof",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    if (selectedPhotoUri == null) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Select", fontSize = 12.sp)
                        }
                    } else {
                        IconButton(onClick = { selectedPhotoUri = null }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Remove photo",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            // Live AI Analysis Assistant Card
            AnimatedVisibility(
                visible = title.isNotBlank() || description.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE9D5FF))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LavishGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Live Analysis",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryBlue
                                )
                            }
                            PriorityBadge(priority = liveAiState.priority)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Category Tag
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(liveAiState.category),
                                        contentDescription = null,
                                        tint = LavishGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = liveAiState.category.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }

                            // Department Tag
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEEF2FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Routing", fontSize = 10.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = liveAiState.department,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryBlue,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Duplicate warning notice if detected
                        if (liveAiState.duplicateInfo.isDuplicate) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFFBEB),
                                modifier = Modifier.fillMaxWidth(),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCD34D))
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Duplicate Alert (${(liveAiState.duplicateInfo.similarityScore * 100).toInt()}% Match)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF92400E)
                                        )
                                        Text(
                                            text = "Similar issue ${liveAiState.duplicateInfo.similarComplaintId ?: ""} already logged in $block.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = {
                    if (block.isNotBlank() && roomNumber.isNotBlank() && title.isNotBlank() && description.isNotBlank()) {
                        if (liveAiState.duplicateInfo.isDuplicate && !showDuplicateDialog) {
                            showDuplicateDialog = true
                        } else {
                            isSubmitting = true
                            viewModel.createComplaint(
                                title = title,
                                description = description,
                                block = block,
                                roomNumber = roomNumber,
                                photoUri = selectedPhotoUri,
                                onSuccess = { id ->
                                    isSubmitting = false
                                    onComplaintCreated(id)
                                }
                            )
                        }
                    }
                },
                enabled = block.isNotBlank() && roomNumber.isNotBlank() && title.isNotBlank() && description.isNotBlank() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_complaint_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Complaint With AI", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    // Duplicate detection confirmation dialog
    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFFD97706)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Similar Complaint Found")
                }
            },
            text = {
                Text(
                    "AI detected that ${liveAiState.duplicateInfo.similarComplaintId} (\"${liveAiState.duplicateInfo.similarComplaintTitle ?: "Water leakage"}\") was already reported for $block with ${(liveAiState.duplicateInfo.similarityScore * 100).toInt()}% similarity.\n\nDo you still want to register a separate complaint?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDuplicateDialog = false
                        isSubmitting = true
                        viewModel.createComplaint(
                            title = title,
                            description = description,
                            block = block,
                            roomNumber = roomNumber,
                            photoUri = selectedPhotoUri,
                            onSuccess = { id ->
                                isSubmitting = false
                                onComplaintCreated(id)
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Submit Anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateDialog = false }) {
                    Text("Review Existing")
                }
            }
        )
    }
}
