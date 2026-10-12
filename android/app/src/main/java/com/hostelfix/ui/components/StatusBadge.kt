package com.hostelfix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hostelfix.data.model.ComplaintStatus
import com.hostelfix.ui.theme.StatusAmber
import com.hostelfix.ui.theme.StatusBlue
import com.hostelfix.ui.theme.StatusGreen
import com.hostelfix.ui.theme.StatusRed

@Composable
fun StatusBadge(status: ComplaintStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        ComplaintStatus.SUBMITTED -> Triple(Color(0xFFE2E8F0), Color(0xFF475569), "Submitted")
        ComplaintStatus.AI_ANALYZED -> Triple(Color(0xFFE0E7FF), Color(0xFF4338CA), "AI Analyzed")
        ComplaintStatus.ASSIGNED -> Triple(Color(0xFFDBEAFE), StatusBlue, "Assigned")
        ComplaintStatus.IN_PROGRESS -> Triple(Color(0xFFFEF3C7), StatusAmber, "In Progress")
        ComplaintStatus.RESOLVED -> Triple(Color(0xFFD1FAE5), StatusGreen, "Resolved")
        ComplaintStatus.FEEDBACK -> Triple(Color(0xFFFCE7F3), Color(0xFFBE185D), "Feedback Needed")
        ComplaintStatus.CLOSED -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), "Closed")
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PriorityBadge(priority: String?, modifier: Modifier = Modifier) {
    val p = priority?.uppercase() ?: "MEDIUM"
    val (bgColor, textColor) = when (p) {
        "CRITICAL" -> Pair(Color(0xFFFEE2E2), StatusRed)
        "HIGH" -> Pair(Color(0xFFFFEDD5), Color(0xFFC2410C))
        "MEDIUM" -> Pair(Color(0xFFFEF3C7), StatusAmber)
        "LOW" -> Pair(Color(0xFFECFDF5), StatusGreen)
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF64748B))
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = p,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
