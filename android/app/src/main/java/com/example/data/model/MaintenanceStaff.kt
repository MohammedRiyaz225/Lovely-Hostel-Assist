package com.example.data.model

data class MaintenanceStaff(
    val id: String,
    val name: String,
    val roleTitle: String,
    val trade: ComplaintCategory,
    val phone: String,
    val rating: Float,
    val activeJobsCount: Int,
    val isAvailable: Boolean = true,
    val shift: String = "Morning (08:00 AM - 05:00 PM)"
)

object MaintenanceStaffDirectory {
    val staffList: List<MaintenanceStaff> = listOf(
        MaintenanceStaff(
            id = "STF-101",
            name = "Ramesh Kumar",
            roleTitle = "Senior Plumber & Pipe Fitter",
            trade = ComplaintCategory.PLUMBING,
            phone = "+91 98140 33211",
            rating = 4.8f,
            activeJobsCount = 2,
            isAvailable = true,
            shift = "General (09:00 AM - 06:00 PM)"
        ),
        MaintenanceStaff(
            id = "STF-102",
            name = "Suresh Patel",
            roleTitle = "Lead Electrical Technician",
            trade = ComplaintCategory.ELECTRICAL,
            phone = "+91 98140 33212",
            rating = 4.9f,
            activeJobsCount = 1,
            isAvailable = true,
            shift = "24x7 Emergency On-Call"
        ),
        MaintenanceStaff(
            id = "STF-103",
            name = "Vikram Singh",
            roleTitle = "Senior Woodcraft & Carpenter",
            trade = ComplaintCategory.FURNITURE,
            phone = "+91 98140 33213",
            rating = 4.7f,
            activeJobsCount = 1,
            isAvailable = true,
            shift = "Morning (08:00 AM - 04:30 PM)"
        ),
        MaintenanceStaff(
            id = "STF-104",
            name = "Karan Verma",
            roleTitle = "Hostel Network & Wi-Fi Engineer",
            trade = ComplaintCategory.WIFI,
            phone = "+91 98140 33214",
            rating = 4.9f,
            activeJobsCount = 0,
            isAvailable = true,
            shift = "Morning (09:00 AM - 06:00 PM)"
        ),
        MaintenanceStaff(
            id = "STF-105",
            name = "Balwinder Singh",
            roleTitle = "Sanitation & Water Cooler Supervisor",
            trade = ComplaintCategory.CLEANING,
            phone = "+91 98140 33215",
            rating = 4.6f,
            activeJobsCount = 1,
            isAvailable = true,
            shift = "Morning (07:00 AM - 03:30 PM)"
        ),
        MaintenanceStaff(
            id = "STF-106",
            name = "Manjit Singh",
            roleTitle = "Drainage & Sewer Specialist",
            trade = ComplaintCategory.PLUMBING,
            phone = "+91 98140 33216",
            rating = 4.8f,
            activeJobsCount = 1,
            isAvailable = true,
            shift = "General (09:00 AM - 06:00 PM)"
        ),
        MaintenanceStaff(
            id = "STF-107",
            name = "Rajesh Gill",
            roleTitle = "Estate General Duty Handyman",
            trade = ComplaintCategory.GENERAL,
            phone = "+91 98140 33217",
            rating = 4.7f,
            activeJobsCount = 0,
            isAvailable = true,
            shift = "Evening (02:00 PM - 10:00 PM)"
        )
    )

    fun findStaffForCategory(category: ComplaintCategory): List<MaintenanceStaff> {
        val matches = staffList.filter { it.trade == category }
        return if (matches.isNotEmpty()) matches else staffList
    }
}
