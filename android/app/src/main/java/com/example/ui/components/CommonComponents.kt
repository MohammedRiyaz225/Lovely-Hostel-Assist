package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AIAnalysis
import com.example.data.model.ComplaintCategory
import com.example.data.model.ComplaintPriority
import com.example.data.model.ComplaintStatus
import com.example.data.model.DuplicateInfo
import com.example.data.model.SentimentType
import com.example.ui.theme.LavishGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PriorityCriticalColor
import com.example.ui.theme.PriorityHighColor
import com.example.ui.theme.PriorityLowColor
import com.example.ui.theme.PriorityMediumColor
import com.example.ui.theme.StatusAssigned
import com.example.ui.theme.StatusInProgress
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusResolved

fun safeDialPhone(context: Context, phoneNumber: String) {
    val sanitized = phoneNumber.replace(" ", "").trim()
    if (sanitized.isBlank()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Phone dialer unavailable on this device ($phoneNumber)", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun PriorityBadge(priority: ComplaintPriority, modifier: Modifier = Modifier) {
    val (color, bgColor, borderColor) = when (priority) {
        ComplaintPriority.CRITICAL -> Triple(PriorityCriticalColor, Color(0xFFFFF1F2), Color(0xFFFECDD3))
        ComplaintPriority.HIGH -> Triple(PriorityHighColor, Color(0xFFFFF7ED), Color(0xFFFED7AA))
        ComplaintPriority.MEDIUM -> Triple(PriorityMediumColor, Color(0xFFFEF3C7), Color(0xFFFDE68A))
        ComplaintPriority.LOW -> Triple(PriorityLowColor, Color(0xFFECFDF5), Color(0xFFA7F3D0))
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${priority.label} Priority",
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusPill(status: ComplaintStatus, modifier: Modifier = Modifier) {
    val (color, bg, border) = when (status) {
        ComplaintStatus.PENDING -> Triple(StatusPending, Color(0xFFFEF3C7), Color(0xFFFDE68A))
        ComplaintStatus.ASSIGNED -> Triple(StatusAssigned, Color(0xFFEFF6FF), Color(0xFFBFDBFE))
        ComplaintStatus.IN_PROGRESS -> Triple(StatusInProgress, Color(0xFFF3E8FF), Color(0xFFDDD6FE))
        ComplaintStatus.RESOLVED -> Triple(StatusResolved, Color(0xFFECFDF5), Color(0xFFA7F3D0))
        ComplaintStatus.REJECTED -> Triple(Color(0xFF64748B), Color(0xFFF8FAFC), Color(0xFFE2E8F0))
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Text(
            text = status.label,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

fun getCategoryIcon(category: ComplaintCategory): ImageVector {
    return when (category) {
        ComplaintCategory.PLUMBING -> Icons.Default.WaterDrop
        ComplaintCategory.ELECTRICAL -> Icons.Default.Bolt
        ComplaintCategory.CLEANING -> Icons.Default.CleaningServices
        ComplaintCategory.WIFI -> Icons.Default.Wifi
        ComplaintCategory.FOOD -> Icons.Default.Restaurant
        ComplaintCategory.FURNITURE -> Icons.Default.Chair
        ComplaintCategory.SECURITY -> Icons.Default.Security
        ComplaintCategory.GENERAL -> Icons.Default.Build
    }
}

@Composable
fun AiInsightCard(
    aiAnalysis: AIAnalysis,
    duplicateInfo: DuplicateInfo? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAF5FF)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE9D5FF))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
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
                        text = "AI Diagnostic Engine",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        fontSize = 14.sp
                    )
                }
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Text(
                        text = "${(aiAnalysis.confidence * 100).toInt()}% match",
                        color = LavishGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "\"${aiAnalysis.summary}\"",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Department
                Column(modifier = Modifier.weight(1f)) {
                    Text("Department", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(
                        text = aiAnalysis.department,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }

                // Sentiment
                Column {
                    Text("Sentiment", fontSize = 11.sp, color = Color(0xFF64748B))
                    val sentimentColor = when (aiAnalysis.sentiment) {
                        SentimentType.NEGATIVE -> PriorityHighColor
                        SentimentType.POSITIVE -> PriorityLowColor
                        SentimentType.NEUTRAL -> Color(0xFF64748B)
                    }
                    Text(
                        text = aiAnalysis.sentiment.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = sentimentColor
                    )
                }

                // ETA
                Column {
                    Text("Target ETA", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(
                        text = "${aiAnalysis.estimatedResolutionHours} hrs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            // Duplicate banner if present
            if (duplicateInfo?.isDuplicate == true) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFBEB),
                    modifier = Modifier.fillMaxWidth(),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFDE68A))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Potential Duplicate: Similar complaint ${duplicateInfo.similarComplaintId ?: ""} detected (${(duplicateInfo.similarityScore * 100).toInt()}% match)",
                            color = Color(0xFF92400E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
