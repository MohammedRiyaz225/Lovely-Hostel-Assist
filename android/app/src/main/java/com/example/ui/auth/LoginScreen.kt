package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.HostelFixViewModel
import com.example.ui.theme.LavishChampagneBorder
import com.example.ui.theme.LavishGold
import com.example.ui.theme.LavishGoldWarm
import com.example.ui.theme.LavishRoyalGradient
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: HostelFixViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var emailOrId by remember { mutableStateOf("CS22B104") }
    var password by remember { mutableStateOf("student123") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun updateDemoCredentials(role: UserRole) {
        selectedRole = role
        errorMessage = null
        when (role) {
            UserRole.STUDENT -> {
                emailOrId = "CS22B104"
                password = "student123"
            }
            UserRole.WARDEN -> {
                emailOrId = "EMP-WD-401"
                password = "warden123"
            }
            UserRole.STAFF -> {
                emailOrId = "EMP-MNT-782"
                password = "staff123"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Lavish Top Header with Royal Indigo Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(LavishRoyalGradient)
                    .padding(horizontal = 24.dp, vertical = 36.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFFFEF3C7).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = null,
                                    tint = LavishGoldWarm,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "HostelFix",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "LPU Hostel Care & Maintenance",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE0E7FF)
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFFFEF3C7).copy(alpha = 0.22f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LavishGoldWarm,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI-Driven",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFEF3C7)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Sign In to Your Hostel Portal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Distinct portal accounts for Students, Wardens, and Maintenance Staff with separate permissions and dashboards.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Role Account Selection Tab Bar
                Text(
                    text = "Select Account Portal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleSelectCard(
                        title = "Student",
                        subtitle = "Resident",
                        icon = Icons.Default.School,
                        isSelected = selectedRole == UserRole.STUDENT,
                        onClick = { updateDemoCredentials(UserRole.STUDENT) },
                        modifier = Modifier.weight(1f)
                    )
                    RoleSelectCard(
                        title = "Warden",
                        subtitle = "Supervisory",
                        icon = Icons.Default.Security,
                        isSelected = selectedRole == UserRole.WARDEN,
                        onClick = { updateDemoCredentials(UserRole.WARDEN) },
                        modifier = Modifier.weight(1f)
                    )
                    RoleSelectCard(
                        title = "Staff",
                        subtitle = "Technician",
                        icon = Icons.Default.Engineering,
                        isSelected = selectedRole == UserRole.STAFF,
                        onClick = { updateDemoCredentials(UserRole.STAFF) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Role Permissions Explainer Banner
                Surface(
                    color = when (selectedRole) {
                        UserRole.STUDENT -> Color(0xFFEEF2FF)
                        UserRole.WARDEN -> Color(0xFFFEF3C7)
                        UserRole.STAFF -> Color(0xFFECFDF5)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            when (selectedRole) {
                                UserRole.STUDENT -> Color(0xFFC7D2FE)
                                UserRole.WARDEN -> LavishChampagneBorder
                                UserRole.STAFF -> Color(0xFFA7F3D0)
                            }
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (selectedRole) {
                                UserRole.STUDENT -> Icons.Default.School
                                UserRole.WARDEN -> Icons.Default.Security
                                UserRole.STAFF -> Icons.Default.Engineering
                            },
                            contentDescription = null,
                            tint = when (selectedRole) {
                                UserRole.STUDENT -> PrimaryBlue
                                UserRole.WARDEN -> LavishGold
                                UserRole.STAFF -> SecondaryTeal
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (selectedRole) {
                                UserRole.STUDENT -> "Student Portal: Log in with your college Registration No. Report room issues, monitor repair status, and talk to Hostel AI."
                                UserRole.WARDEN -> "Warden Desk: Log in with your Employee ID. Triage block complaints, dispatch staff, review duplicates, and manage SLAs."
                                UserRole.STAFF -> "Field Operations: Log in with your Staff ID / Employee ID. View assigned repairs, record maintenance logs, and sign off tickets."
                            },
                            fontSize = 11.sp,
                            color = when (selectedRole) {
                                UserRole.STUDENT -> Color(0xFF1E293B)
                                UserRole.WARDEN -> Color(0xFF78350F)
                                UserRole.STAFF -> Color(0xFF065F46)
                            },
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Login Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Login Credentials",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Registration No. or Employee/Staff ID field
                        Text(
                            text = when (selectedRole) {
                                UserRole.STUDENT -> "Registration Number (User ID)"
                                UserRole.WARDEN -> "Employee ID (User ID)"
                                UserRole.STAFF -> "Staff ID / Employee ID (User ID)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = emailOrId,
                            onValueChange = {
                                emailOrId = it
                                errorMessage = null
                            },
                            placeholder = {
                                Text(
                                    when (selectedRole) {
                                        UserRole.STUDENT -> "e.g. CS22B104 or your Registration No."
                                        UserRole.WARDEN -> "e.g. EMP-WD-401 or your Employee ID"
                                        UserRole.STAFF -> "e.g. EMP-MNT-782 or your Staff ID"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password field with self-chosen password highlight
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Password",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Self-chosen password",
                                fontSize = 11.sp,
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            placeholder = { Text("Enter your self-chosen password") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (emailOrId.isNotBlank() && password.isNotBlank()) {
                                    isLoading = true
                                    viewModel.login(emailOrId, password, selectedRole, onSuccess = {
                                        isLoading = false
                                        onLoginSuccess()
                                    }, onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    })
                                }
                            }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )

                        // Error message display
                        AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                            errorMessage?.let { err ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = err,
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (emailOrId.isBlank()) {
                                    errorMessage = when (selectedRole) {
                                        UserRole.STUDENT -> "Please enter your Registration Number"
                                        UserRole.WARDEN -> "Please enter your Employee ID"
                                        UserRole.STAFF -> "Please enter your Staff ID / Employee ID"
                                    }
                                    return@Button
                                }
                                if (password.isBlank()) {
                                    errorMessage = "Please enter your self-chosen password"
                                    return@Button
                                }
                                isLoading = true
                                viewModel.login(emailOrId, password, selectedRole, onSuccess = {
                                    isLoading = false
                                    onLoginSuccess()
                                }, onError = { err ->
                                    isLoading = false
                                    errorMessage = err
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = LavishGoldWarm, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sign In as ${selectedRole.displayName}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick-Fill Sample College IDs Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE9D5FF))
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = LavishGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sample College USER_IDs (Any password works)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DemoQuickButton(
                                label = "CS22B104 (Reg. No.)",
                                isSelected = selectedRole == UserRole.STUDENT,
                                onClick = { updateDemoCredentials(UserRole.STUDENT) },
                                modifier = Modifier.weight(1f)
                            )
                            DemoQuickButton(
                                label = "EMP-WD-401 (Emp. ID)",
                                isSelected = selectedRole == UserRole.WARDEN,
                                onClick = { updateDemoCredentials(UserRole.WARDEN) },
                                modifier = Modifier.weight(1f)
                            )
                            DemoQuickButton(
                                label = "EMP-MNT-782 (Staff ID)",
                                isSelected = selectedRole == UserRole.STAFF,
                                onClick = { updateDemoCredentials(UserRole.STAFF) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun RoleSelectCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("role_select_${title.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) PrimaryBlue else Color(0xFFCBD5E1)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) LavishGoldWarm else Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isSelected) Color.White else Color(0xFF0F172A)
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isSelected) Color(0xFFE0E7FF) else Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun DemoQuickButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) PrimaryBlue else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryBlue else Color(0xFFCBD5E1))
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF334155)
            )
        }
    }
}
