package com.example.data.repository

import com.example.data.ai.HostelFixAIEngine
import com.example.data.api.ApiClient
import com.example.data.api.ComplaintRequest
import com.example.data.api.StatusUpdateRequest
import com.example.data.local.ComplaintDao
import com.example.data.local.ComplaintEntity
import com.example.data.model.AIAnalysis
import com.example.data.model.Complaint
import com.example.data.model.ComplaintStatus
import com.example.data.model.DuplicateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ComplaintRepository(
    private val complaintDao: ComplaintDao
) {

    val allComplaints: Flow<List<Complaint>> = complaintDao.getAllComplaints().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun ensureInitialSeedData() = withContext(Dispatchers.IO) {
        try {
            if (complaintDao.getComplaintCount() == 0) {
                com.example.data.local.AppDatabase.populateInitialData(complaintDao)
            }
        } catch (_: Exception) {
        }
    }

    suspend fun getComplaintById(id: String): Complaint? = withContext(Dispatchers.IO) {
        complaintDao.getComplaintById(id)?.toDomain()
    }

    suspend fun runAiPreCheck(
        title: String,
        description: String,
        block: String
    ): Pair<AIAnalysis, DuplicateInfo> = withContext(Dispatchers.IO) {
        val currentList = allComplaints.firstOrNull() ?: emptyList()
        val analysis = HostelFixAIEngine.runFullPipeline(title, description, block, currentList)
        val duplicateInfo = HostelFixAIEngine.detectDuplicates(title, description, block, currentList)
        Pair(analysis, duplicateInfo)
    }

    companion object {
        private val complaintSequence = java.util.concurrent.atomic.AtomicInteger(0)
    }

    suspend fun createComplaint(
        title: String,
        description: String,
        block: String,
        roomNumber: String,
        studentName: String,
        studentRoll: String,
        photoUri: String? = null
    ): Complaint = withContext(Dispatchers.IO) {
        val currentList = allComplaints.firstOrNull() ?: emptyList()
        val aiAnalysis = HostelFixAIEngine.runFullPipeline(title, description, block, currentList)
        val duplicateInfo = HostelFixAIEngine.detectDuplicates(title, description, block, currentList)

        val existingIds = currentList.map { it.id }.toSet()
        var candidateId: String
        do {
            val suffix = (2000 + ((System.currentTimeMillis() + complaintSequence.incrementAndGet()) % 7999))
            candidateId = "CMP-$suffix"
        } while (candidateId in existingIds)
        val newId = candidateId

        val complaint = Complaint(
            id = newId,
            title = title,
            description = description,
            block = block,
            roomNumber = roomNumber,
            studentName = studentName,
            studentRoll = studentRoll,
            status = ComplaintStatus.PENDING,
            assignedTo = null,
            assignedStaffRole = null,
            createdAt = System.currentTimeMillis(),
            resolvedAt = null,
            photoUri = photoUri,
            aiAnalysis = aiAnalysis,
            duplicateInfo = duplicateInfo
        )

        // Save locally in Room
        complaintDao.insertComplaint(ComplaintEntity.fromDomain(complaint))

        // Attempt to sync to FastAPI backend asynchronously
        try {
            ApiClient.apiService.createComplaint(
                ComplaintRequest(
                    title = title,
                    description = description,
                    block = block,
                    room_number = roomNumber,
                    category = aiAnalysis.category.name,
                    priority = aiAnalysis.priority.name,
                    image_url = photoUri
                )
            )
        } catch (_: Exception) {
            // Graceful offline fallback
        }

        complaint
    }

    suspend fun updateStatus(
        id: String,
        newStatus: ComplaintStatus,
        assignedTo: String? = null,
        note: String? = null
    ) = withContext(Dispatchers.IO) {
        val resolvedTimestamp = if (newStatus == ComplaintStatus.RESOLVED || newStatus == ComplaintStatus.REJECTED) {
            System.currentTimeMillis()
        } else {
            null
        }
        if (note != null) {
            complaintDao.updateStatusWithNote(id, newStatus.name, assignedTo, note, resolvedTimestamp)
        } else {
            complaintDao.updateStatus(id, newStatus.name, assignedTo, resolvedTimestamp)
        }
        try {
            ApiClient.apiService.updateComplaintStatus(
                id = id,
                request = StatusUpdateRequest(
                    status = newStatus.name,
                    assigned_to = assignedTo,
                    note = note ?: "Updated from HostelFix app"
                )
            )
        } catch (_: Exception) {
            // Offline fallback
        }
    }

    suspend fun addWorkNote(
        id: String,
        note: String
    ) = withContext(Dispatchers.IO) {
        complaintDao.updateWorkNotes(id, note)
    }

    suspend fun submitFeedback(
        id: String,
        rating: Int,
        comment: String
    ) = withContext(Dispatchers.IO) {
        complaintDao.addFeedback(id, rating, comment)
    }

    suspend fun syncWithBackend() = withContext(Dispatchers.IO) {
        try {
            val remoteList = ApiClient.apiService.getComplaints()
            if (remoteList.isNotEmpty()) {
                // Ingest remote complaints
                remoteList.forEach { remote ->
                    val existing = complaintDao.getComplaintById(remote.id)
                    if (existing == null) {
                        val aiAnalysis = HostelFixAIEngine.runFullPipeline(
                            remote.title,
                            remote.description,
                            remote.block
                        )
                        val entity = ComplaintEntity(
                            id = remote.id,
                            title = remote.title,
                            description = remote.description,
                            block = remote.block,
                            roomNumber = remote.room_number ?: "",
                            studentName = remote.student_name ?: "Resident Student",
                            studentRoll = remote.student_roll ?: "N/A",
                            status = remote.status,
                            assignedTo = remote.assigned_to,
                            assignedStaffRole = null,
                            createdAt = System.currentTimeMillis(),
                            resolvedAt = null,
                            photoUri = null,
                            category = remote.ai?.category ?: aiAnalysis.category.name,
                            priority = remote.ai?.priority ?: aiAnalysis.priority.name,
                            department = remote.ai?.department ?: aiAnalysis.department,
                            sentiment = remote.ai?.sentiment ?: aiAnalysis.sentiment.name,
                            aiConfidence = remote.ai?.confidence ?: aiAnalysis.confidence,
                            aiSummary = remote.ai?.summary ?: aiAnalysis.summary,
                            estimatedResolutionHours = aiAnalysis.estimatedResolutionHours,
                            isDuplicate = remote.duplicate?.is_duplicate ?: false,
                            similarComplaintId = remote.duplicate?.similar_complaint_id,
                            similarComplaintTitle = null,
                            similarityScore = remote.duplicate?.similarity ?: 0f,
                            studentRating = null,
                            studentFeedbackComment = null
                        )
                        complaintDao.insertComplaint(entity)
                    }
                }
            }
        } catch (_: Exception) {
            // Server offline, Room handles local data seamlessly
        }
    }
}
