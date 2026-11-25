package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ComplaintEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun complaintDao(): ComplaintDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hostelfix_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(dao: ComplaintDao) {
            val now = System.currentTimeMillis()
                val oneHour = 3600_000L
                val oneDay = 86400_000L

                val seedComplaints = listOf(
                    ComplaintEntity(
                        id = "CMP-1021",
                        title = "Water leakage in washroom tap",
                        description = "Water has been leaking continuously from tap #3 in BH-4 (Boys Hostel 4) 2nd floor washroom for three days. Floor is flooded.",
                        block = "BH-4",
                        roomNumber = "Washroom 2F",
                        studentName = "Rahul Sharma",
                        studentRoll = "CS22B088",
                        status = "ASSIGNED",
                        assignedTo = "Ramesh Kumar (Plumber)",
                        assignedStaffRole = "Senior Plumber",
                        createdAt = now - (oneDay * 3),
                        resolvedAt = null,
                        photoUri = null,
                        category = "PLUMBING",
                        priority = "HIGH",
                        department = "Maintenance (Plumbing Services)",
                        sentiment = "NEGATIVE",
                        aiConfidence = 0.94f,
                        aiSummary = "Recurring water leakage problem reported in BH-4 washroom.",
                        estimatedResolutionHours = 12,
                        isDuplicate = false,
                        similarComplaintId = null,
                        similarComplaintTitle = null,
                        similarityScore = 0.0f,
                        studentRating = null,
                        studentFeedbackComment = null
                    ),
                    ComplaintEntity(
                        id = "CMP-1022",
                        title = "Ceiling fan making loud grinding noise",
                        description = "The fan in room 204 of BH-4 shakes violently on speed 4 and makes an unbearable grinding screech. Difficult to sleep and study.",
                        block = "BH-4",
                        roomNumber = "204",
                        studentName = "Alex Rivera",
                        studentRoll = "CS22B104",
                        status = "IN_PROGRESS",
                        assignedTo = "Suresh Patel (Electrician)",
                        assignedStaffRole = "Electrical Technician",
                        createdAt = now - (oneDay * 1),
                        resolvedAt = null,
                        photoUri = null,
                        category = "ELECTRICAL",
                        priority = "MEDIUM",
                        department = "Electrical & Power Maintenance",
                        sentiment = "NEGATIVE",
                        aiConfidence = 0.91f,
                        aiSummary = "Lighting or ceiling fan fixture defect identified in BH-4.",
                        estimatedResolutionHours = 24,
                        isDuplicate = false,
                        similarComplaintId = null,
                        similarComplaintTitle = null,
                        similarityScore = 0.0f,
                        studentRating = null,
                        studentFeedbackComment = null
                    ),
                    ComplaintEntity(
                        id = "CMP-1023",
                        title = "Wi-Fi access point disconnected",
                        description = "Hostel Wi-Fi SSID LPU_Hostel_HighSpeed has zero internet access and keeps disconnecting every 2 minutes on BH-1 Wing C.",
                        block = "BH-1",
                        roomNumber = "Wing C",
                        studentName = "Priya Nair",
                        studentRoll = "EC22B042",
                        status = "RESOLVED",
                        assignedTo = "Karan Verma (Network Admin)",
                        assignedStaffRole = "Network Administrator",
                        createdAt = now - (oneDay * 4),
                        resolvedAt = now - (oneHour * 5),
                        photoUri = null,
                        category = "WIFI",
                        priority = "HIGH",
                        department = "Campus IT & Network Operations",
                        sentiment = "NEUTRAL",
                        aiConfidence = 0.89f,
                        aiSummary = "Network connectivity outage affecting residents in BH-1.",
                        estimatedResolutionHours = 6,
                        isDuplicate = false,
                        similarComplaintId = null,
                        similarComplaintTitle = null,
                        similarityScore = 0.0f,
                        studentRating = 5,
                        studentFeedbackComment = "Rebooted router switch quickly. Thanks team!"
                    ),
                    ComplaintEntity(
                        id = "CMP-1024",
                        title = "Study table drawer lock broken",
                        description = "Wooden drawer lock mechanism came off in BH-7 room 118. Unable to secure laptop and notes.",
                        block = "BH-7",
                        roomNumber = "118",
                        studentName = "Dev Patel",
                        studentRoll = "ME22B015",
                        status = "PENDING",
                        assignedTo = null,
                        assignedStaffRole = null,
                        createdAt = now - (oneHour * 3),
                        resolvedAt = null,
                        photoUri = null,
                        category = "FURNITURE",
                        priority = "LOW",
                        department = "Carpentry & Civil Maintenance",
                        sentiment = "NEUTRAL",
                        aiConfidence = 0.86f,
                        aiSummary = "Damaged room furniture or woodwork item needing repair in BH-7.",
                        estimatedResolutionHours = 48,
                        isDuplicate = false,
                        similarComplaintId = null,
                        similarComplaintTitle = null,
                        similarityScore = 0.0f,
                        studentRating = null,
                        studentFeedbackComment = null
                    ),
                    ComplaintEntity(
                        id = "CMP-1025",
                        title = "Geyser water heater tripping circuit breaker",
                        description = "In GH-2 (Girls Hostel 2) 3rd floor washroom, turning on the geyser instantly trips the main MCB breaker switch with a spark.",
                        block = "GH-2",
                        roomNumber = "Washroom 3F",
                        studentName = "Ananya Sen",
                        studentRoll = "BT22B019",
                        status = "PENDING",
                        assignedTo = null,
                        assignedStaffRole = null,
                        createdAt = now - (oneHour * 2),
                        resolvedAt = null,
                        photoUri = null,
                        category = "ELECTRICAL",
                        priority = "CRITICAL",
                        department = "Electrical & Power Maintenance",
                        sentiment = "NEGATIVE",
                        aiConfidence = 0.96f,
                        aiSummary = "Critical electrical short circuit hazard identified in GH-2 geyser unit.",
                        estimatedResolutionHours = 4,
                        isDuplicate = false,
                        similarComplaintId = null,
                        similarComplaintTitle = null,
                        similarityScore = 0.0f,
                        studentRating = null,
                        studentFeedbackComment = null
                    ),
                    ComplaintEntity(
                        id = "CMP-1026",
                        title = "Kitchen sink drainage blocked in apartment",
                        description = "In Uni-Living Tower 2 Apt 405, sink wastewater is backing up onto the kitchen counter. Need immediate plumbing intervention.",
                        block = "Uni-Living",
                        roomNumber = "Apt 405",
                        studentName = "Simran Gill",
                        studentRoll = "BA22B062",
                        status = "IN_PROGRESS",
                        assignedTo = "Manjit Singh (Plumber)",
                        assignedStaffRole = "Campus Plumber",
                        createdAt = now - (oneDay * 1),
                        resolvedAt = null,
                        photoUri = null,
                        category = "PLUMBING",
                        priority = "HIGH",
                        department = "Maintenance (Plumbing Services)",
                        sentiment = "NEGATIVE",
                        aiConfidence = 0.93f,
                        aiSummary = "Severe plumbing clog and drainage blockage in Uni-Living apartments.",
                        estimatedResolutionHours = 8,
                        isDuplicate = false,
                        similarComplaintId = null,
                        similarComplaintTitle = null,
                        similarityScore = 0.0f,
                        studentRating = null,
                        studentFeedbackComment = null
                    ),
                    ComplaintEntity(
                        id = "CMP-1027",
                        title = "Corridor water cooler foul odor",
                        description = "Water cooler on 1st floor of GH-5 has a pungent odor and dirty filter cartridge. Students unable to drink water.",
                        block = "GH-5",
                        roomNumber = "1st Floor Hall",
                        studentName = "Pooja Verma",
                        studentRoll = "MB22B007",
                        status = "ASSIGNED",
                        assignedTo = "Balwinder Singh (Sanitation)",
                        assignedStaffRole = "Sanitation Supervisor",
                        createdAt = now - (oneDay * 2),
                        resolvedAt = null,
                        photoUri = null,
                        category = "CLEANING",
                        priority = "HIGH",
                        department = "Sanitation & Housekeeping",
                        sentiment = "NEGATIVE",
                        aiConfidence = 0.92f,
                        aiSummary = "Hygiene concern reported in GH-5 drinking water supply point.",
                        estimatedResolutionHours = 12,
                        isDuplicate = false,
                        similarComplaintId = null,
                        similarComplaintTitle = null,
                        similarityScore = 0.0f,
                        studentRating = null,
                        studentFeedbackComment = null
                    )
                )

                dao.insertAll(seedComplaints)
        }
    }
}
