package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AIAnalysis
import com.example.data.model.Complaint
import com.example.data.model.ComplaintCategory
import com.example.data.model.ComplaintPriority
import com.example.data.model.ComplaintStatus
import com.example.data.model.DuplicateInfo
import com.example.data.model.SentimentType
import com.example.data.model.StatusStep
import com.example.data.model.StudentFeedback

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val block: String,
    val roomNumber: String,
    val studentName: String,
    val studentRoll: String,
    val status: String,
    val assignedTo: String?,
    val assignedStaffRole: String?,
    val createdAt: Long,
    val resolvedAt: Long?,
    val photoUri: String?,
    val category: String,
    val priority: String,
    val department: String,
    val sentiment: String,
    val aiConfidence: Float,
    val aiSummary: String,
    val estimatedResolutionHours: Int,
    val isDuplicate: Boolean,
    val similarComplaintId: String?,
    val similarComplaintTitle: String?,
    val similarityScore: Float,
    val studentRating: Int?,
    val studentFeedbackComment: String?,
    val workNotes: String? = null
) {
    fun toDomain(): Complaint {
        val cat = try {
            ComplaintCategory.valueOf(category)
        } catch (_: Exception) {
            ComplaintCategory.GENERAL
        }
        val prio = try {
            ComplaintPriority.valueOf(priority)
        } catch (_: Exception) {
            ComplaintPriority.MEDIUM
        }
        val sent = try {
            SentimentType.valueOf(sentiment)
        } catch (_: Exception) {
            SentimentType.NEUTRAL
        }
        val stat = try {
            ComplaintStatus.valueOf(status)
        } catch (_: Exception) {
            ComplaintStatus.PENDING
        }

        val history = mutableListOf(
            StatusStep(ComplaintStatus.PENDING, createdAt, "Complaint filed by $studentName ($studentRoll) • Room $roomNumber")
        )
        if (assignedTo != null) {
            history.add(StatusStep(ComplaintStatus.ASSIGNED, createdAt + 1800_000L, "Assigned to $assignedTo"))
        }
        if (stat == ComplaintStatus.IN_PROGRESS || stat == ComplaintStatus.RESOLVED) {
            history.add(StatusStep(ComplaintStatus.IN_PROGRESS, createdAt + 3600_000L, "Repair work underway on premises"))
        }
        if (stat == ComplaintStatus.RESOLVED) {
            history.add(StatusStep(ComplaintStatus.RESOLVED, resolvedAt ?: (createdAt + 7200_000L), workNotes ?: "Maintenance completed and verified"))
        } else if (stat == ComplaintStatus.REJECTED) {
            history.add(StatusStep(ComplaintStatus.REJECTED, resolvedAt ?: (createdAt + 5400_000L), workNotes ?: "Ticket rejected or withdrawn"))
        }
        if (workNotes != null && stat != ComplaintStatus.RESOLVED && stat != ComplaintStatus.REJECTED) {
            history.add(StatusStep(stat, createdAt + 4200_000L, "Staff Log: $workNotes"))
        }
        if (studentRating != null) {
            history.add(StatusStep(ComplaintStatus.RESOLVED, (resolvedAt ?: createdAt) + 1800_000L, "Student rated $studentRating★: \"${studentFeedbackComment.orEmpty()}\""))
        }

        return Complaint(
            id = id,
            title = title,
            description = description,
            block = block,
            roomNumber = roomNumber,
            studentName = studentName,
            studentRoll = studentRoll,
            status = stat,
            assignedTo = assignedTo,
            assignedStaffRole = assignedStaffRole,
            createdAt = createdAt,
            resolvedAt = resolvedAt,
            photoUri = photoUri,
            aiAnalysis = AIAnalysis(
                category = cat,
                priority = prio,
                department = department,
                sentiment = sent,
                confidence = aiConfidence,
                summary = aiSummary,
                estimatedResolutionHours = estimatedResolutionHours
            ),
            duplicateInfo = DuplicateInfo(
                isDuplicate = isDuplicate,
                similarComplaintId = similarComplaintId,
                similarComplaintTitle = similarComplaintTitle,
                similarityScore = similarityScore
            ),
            studentFeedback = if (studentRating != null) {
                StudentFeedback(rating = studentRating, comment = studentFeedbackComment.orEmpty())
            } else null,
            statusHistory = history,
            workNotes = workNotes
        )
    }

    companion object {
        fun fromDomain(complaint: Complaint): ComplaintEntity {
            return ComplaintEntity(
                id = complaint.id,
                title = complaint.title,
                description = complaint.description,
                block = complaint.block,
                roomNumber = complaint.roomNumber,
                studentName = complaint.studentName,
                studentRoll = complaint.studentRoll,
                status = complaint.status.name,
                assignedTo = complaint.assignedTo,
                assignedStaffRole = complaint.assignedStaffRole,
                createdAt = complaint.createdAt,
                resolvedAt = complaint.resolvedAt,
                photoUri = complaint.photoUri,
                category = complaint.aiAnalysis.category.name,
                priority = complaint.aiAnalysis.priority.name,
                department = complaint.aiAnalysis.department,
                sentiment = complaint.aiAnalysis.sentiment.name,
                aiConfidence = complaint.aiAnalysis.confidence,
                aiSummary = complaint.aiAnalysis.summary,
                estimatedResolutionHours = complaint.aiAnalysis.estimatedResolutionHours,
                isDuplicate = complaint.duplicateInfo?.isDuplicate ?: false,
                similarComplaintId = complaint.duplicateInfo?.similarComplaintId,
                similarComplaintTitle = complaint.duplicateInfo?.similarComplaintTitle,
                similarityScore = complaint.duplicateInfo?.similarityScore ?: 0f,
                studentRating = complaint.studentFeedback?.rating,
                studentFeedbackComment = complaint.studentFeedback?.comment,
                workNotes = complaint.workNotes
            )
        }
    }
}
