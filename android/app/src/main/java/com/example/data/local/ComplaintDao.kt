package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {

    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE id = :id LIMIT 1")
    suspend fun getComplaintById(id: String): ComplaintEntity?

    @Query("SELECT * FROM complaints WHERE block = :block ORDER BY createdAt DESC")
    suspend fun getComplaintsByBlock(block: String): List<ComplaintEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(complaints: List<ComplaintEntity>)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)

    @Query("UPDATE complaints SET status = :status, assignedTo = :assignedTo, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, assignedTo: String?, resolvedAt: Long?)

    @Query("UPDATE complaints SET studentRating = :rating, studentFeedbackComment = :comment WHERE id = :id")
    suspend fun addFeedback(id: String, rating: Int, comment: String)

    @Query("UPDATE complaints SET workNotes = :notes WHERE id = :id")
    suspend fun updateWorkNotes(id: String, notes: String)

    @Query("UPDATE complaints SET status = :status, assignedTo = :assignedTo, workNotes = COALESCE(:notes, workNotes), resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun updateStatusWithNote(id: String, status: String, assignedTo: String?, notes: String?, resolvedAt: Long?)

    @Query("SELECT COUNT(*) FROM complaints")
    suspend fun getComplaintCount(): Int

    @Query("DELETE FROM complaints WHERE id = :id")
    suspend fun deleteComplaintById(id: String)
}
