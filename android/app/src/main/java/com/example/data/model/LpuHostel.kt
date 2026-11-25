package com.example.data.model

enum class LpuHostelType(val displayName: String) {
    BOYS("Boys Hostel"),
    GIRLS("Girls Hostel"),
    APARTMENTS("Apartments & Towers")
}

data class LpuHostel(
    val code: String,
    val name: String,
    val type: LpuHostelType,
    val description: String,
    val totalFloors: Int,
    val wings: List<String>,
    val wardenName: String,
    val wardenPhone: String,
    val caretakerPhone: String,
    val nearestLandmark: String,
    val messAttached: String
)

object LpuHostelDirectory {

    val allHostels: List<LpuHostel> = listOf(
        // Boys Hostels (BH-1 to BH-8)
        LpuHostel(
            code = "BH-1",
            name = "Boys Hostel 1",
            type = LpuHostelType.BOYS,
            description = "Central boys residential hostel adjacent to Block 34 & 35.",
            totalFloors = 8,
            wings = listOf("Wing A", "Wing B", "Wing C"),
            wardenName = "Dr. Rajeev Sharma",
            wardenPhone = "+91 98123 10001",
            caretakerPhone = "+91 98700 10001",
            nearestLandmark = "Block 34 Academic Complex",
            messAttached = "Central Boys Mess 1"
        ),
        LpuHostel(
            code = "BH-2",
            name = "Boys Hostel 2",
            type = LpuHostelType.BOYS,
            description = "Multi-wing residence near Olympic sports ground.",
            totalFloors = 8,
            wings = listOf("Wing A", "Wing B", "Wing C", "Wing D"),
            wardenName = "Prof. Manoj Verma",
            wardenPhone = "+91 98123 10002",
            caretakerPhone = "+91 98700 10002",
            nearestLandmark = "Outdoor Sports Complex & Track",
            messAttached = "Central Boys Mess 2"
        ),
        LpuHostel(
            code = "BH-3",
            name = "Boys Hostel 3",
            type = LpuHostelType.BOYS,
            description = "Residence close to the student indoor sports stadium.",
            totalFloors = 7,
            wings = listOf("Wing A", "Wing B", "Wing C"),
            wardenName = "Mr. Amit Pathak",
            wardenPhone = "+91 98123 10003",
            caretakerPhone = "+91 98700 10003",
            nearestLandmark = "Indoor Stadium & Gym",
            messAttached = "Boys Mess 3"
        ),
        LpuHostel(
            code = "BH-4",
            name = "Boys Hostel 4",
            type = LpuHostelType.BOYS,
            description = "Prime boys hostel tower directly facing Uni-Mall and food street.",
            totalFloors = 9,
            wings = listOf("Wing A", "Wing B", "Wing C", "Wing D"),
            wardenName = "Dr. Sandeep Kalia",
            wardenPhone = "+91 98123 10004",
            caretakerPhone = "+91 98700 10004",
            nearestLandmark = "Uni-Mall & Food Kiosks",
            messAttached = "Mess 4 (South & North Wing)"
        ),
        LpuHostel(
            code = "BH-5",
            name = "Boys Hostel 5",
            type = LpuHostelType.BOYS,
            description = "Senior engineering & diploma student residence.",
            totalFloors = 8,
            wings = listOf("Wing A", "Wing B", "Wing C"),
            wardenName = "Mr. Vikramaditya Singh",
            wardenPhone = "+91 98123 10005",
            caretakerPhone = "+91 98700 10005",
            nearestLandmark = "Mechanical Labs & Workshop Block",
            messAttached = "Boys Mess 5"
        ),
        LpuHostel(
            code = "BH-6",
            name = "Boys Hostel 6",
            type = LpuHostelType.BOYS,
            description = "Modern residential tower with Wi-Fi study lounges.",
            totalFloors = 8,
            wings = listOf("Wing A", "Wing B", "Wing C"),
            wardenName = "Dr. Gurpreet Singh",
            wardenPhone = "+91 98123 10006",
            caretakerPhone = "+91 98700 10006",
            nearestLandmark = "Baldev Raj Mittal Unipolis",
            messAttached = "Boys Mess 6"
        ),
        LpuHostel(
            code = "BH-7",
            name = "Boys Hostel 7",
            type = LpuHostelType.BOYS,
            description = "North campus boys hostel near Gate 3.",
            totalFloors = 8,
            wings = listOf("Wing A", "Wing B", "Wing C"),
            wardenName = "Mr. Nitin Joshi",
            wardenPhone = "+91 98123 10007",
            caretakerPhone = "+91 98700 10007",
            nearestLandmark = "Campus Gate 3 & Agriculture Farms",
            messAttached = "Boys Mess 7"
        ),
        LpuHostel(
            code = "BH-8",
            name = "Boys Hostel 8",
            type = LpuHostelType.BOYS,
            description = "High-capacity residential hostel with quiet study zones.",
            totalFloors = 9,
            wings = listOf("Wing A", "Wing B", "Wing C", "Wing D"),
            wardenName = "Prof. Arvind Tiwari",
            wardenPhone = "+91 98123 10008",
            caretakerPhone = "+91 98700 10008",
            nearestLandmark = "North Football Ground",
            messAttached = "Boys Mess 8"
        ),

        // Girls Hostels (GH-1 to GH-6)
        LpuHostel(
            code = "GH-1",
            name = "Girls Hostel 1",
            type = LpuHostelType.GIRLS,
            description = "Main girls hostel enclave next to Uni-Hospital and central lawn.",
            totalFloors = 8,
            wings = listOf("Wing A", "Wing B", "Wing C"),
            wardenName = "Dr. Sunita Kapoor",
            wardenPhone = "+91 98123 20001",
            caretakerPhone = "+91 98700 20001",
            nearestLandmark = "Uni-Hospital & Medical Emergency",
            messAttached = "Girls Mess 1"
        ),
        LpuHostel(
            code = "GH-2",
            name = "Girls Hostel 2",
            type = LpuHostelType.GIRLS,
            description = "Spacious girls residence with reading rooms and badminton court.",
            totalFloors = 8,
            wings = listOf("Wing A", "Wing B", "Wing C", "Wing D"),
            wardenName = "Mrs. Neha Chawla",
            wardenPhone = "+91 98123 20002",
            caretakerPhone = "+91 98700 20002",
            nearestLandmark = "Central Girls Lawn & Library Walkway",
            messAttached = "Girls Mess 2"
        ),
        LpuHostel(
            code = "GH-3",
            name = "Girls Hostel 3",
            type = LpuHostelType.GIRLS,
            description = "Senior girls hostel tower with indoor recreational activity hall.",
            totalFloors = 8,
            wings = listOf("Wing A", "Wing B", "Wing C"),
            wardenName = "Dr. Ananya Roy",
            wardenPhone = "+91 98123 20003",
            caretakerPhone = "+91 98700 20003",
            nearestLandmark = "Block 28 & Management Block",
            messAttached = "Girls Mess 3"
        ),
        LpuHostel(
            code = "GH-4",
            name = "Girls Hostel 4",
            type = LpuHostelType.GIRLS,
            description = "Near Uni-Mall girls residential wing with round-the-clock security.",
            totalFloors = 8,
            wings = listOf("Wing A", "Wing B", "Wing C"),
            wardenName = "Mrs. Manpreet Kaur",
            wardenPhone = "+91 98123 20004",
            caretakerPhone = "+91 98700 20004",
            nearestLandmark = "Uni-Mall Promenade",
            messAttached = "Girls Mess 4"
        ),
        LpuHostel(
            code = "GH-5",
            name = "Girls Hostel 5",
            type = LpuHostelType.GIRLS,
            description = "Modern tower featuring automated laundry and solar heating.",
            totalFloors = 9,
            wings = listOf("Wing A", "Wing B", "Wing C"),
            wardenName = "Dr. Ritu Malhotra",
            wardenPhone = "+91 98123 20005",
            caretakerPhone = "+91 98700 20005",
            nearestLandmark = "East Botanical Garden",
            messAttached = "Girls Mess 5"
        ),
        LpuHostel(
            code = "GH-6",
            name = "Girls Hostel 6",
            type = LpuHostelType.GIRLS,
            description = "Peaceful girls residence near the university central library.",
            totalFloors = 8,
            wings = listOf("Wing A", "Wing B", "Wing C"),
            wardenName = "Prof. Shalini Bansal",
            wardenPhone = "+91 98123 20006",
            caretakerPhone = "+91 98700 20006",
            nearestLandmark = "Central Library & Shanti Devi Mittal Wing",
            messAttached = "Girls Mess 6"
        ),

        // Uni-Living, Apartments & Blocks
        LpuHostel(
            code = "Uni-Living",
            name = "Uni-Living Apartments",
            type = LpuHostelType.APARTMENTS,
            description = "Premium studio, 1BHK & 2BHK serviced student apartments.",
            totalFloors = 10,
            wings = listOf("Tower 1", "Tower 2", "Tower 3"),
            wardenName = "Mr. Deepak Saxena",
            wardenPhone = "+91 98123 30001",
            caretakerPhone = "+91 98700 30001",
            nearestLandmark = "Main Promenade / Uni-Mall Walk",
            messAttached = "Uni-Living Dining Court"
        ),
        LpuHostel(
            code = "Block 34",
            name = "Block 34 Residence",
            type = LpuHostelType.APARTMENTS,
            description = "Heritage academic and student residence complex.",
            totalFloors = 6,
            wings = listOf("Wing A", "Wing B"),
            wardenName = "Mr. Sanjay Gupta",
            wardenPhone = "+91 98123 30034",
            caretakerPhone = "+91 98700 30034",
            nearestLandmark = "Block 33 & 35 Quadrangle",
            messAttached = "Block 34 Cafe & Canteen"
        ),
        LpuHostel(
            code = "Block 36",
            name = "Block 36 Residence",
            type = LpuHostelType.APARTMENTS,
            description = "Student scholar residence adjacent to engineering labs.",
            totalFloors = 6,
            wings = listOf("Wing A", "Wing B"),
            wardenName = "Mr. Harish Chandra",
            wardenPhone = "+91 98123 30036",
            caretakerPhone = "+91 98700 30036",
            nearestLandmark = "Block 36 IT Labs",
            messAttached = "Engineering Canteen 36"
        ),
        LpuHostel(
            code = "Block 38",
            name = "Block 38 Residence",
            type = LpuHostelType.APARTMENTS,
            description = "Design, law and multidisciplinary scholar residence.",
            totalFloors = 7,
            wings = listOf("Wing A", "Wing B"),
            wardenName = "Dr. Tarun Nanda",
            wardenPhone = "+91 98123 30038",
            caretakerPhone = "+91 98700 30038",
            nearestLandmark = "Block 38 Law Department",
            messAttached = "Law & Design Cafeteria"
        ),
        LpuHostel(
            code = "Block 55",
            name = "Block 55 Senior Residence",
            type = LpuHostelType.APARTMENTS,
            description = "Postgraduate, doctorate and senior researcher residence.",
            totalFloors = 7,
            wings = listOf("Wing A", "Wing B"),
            wardenName = "Prof. B.S. Minhas",
            wardenPhone = "+91 98123 30055",
            caretakerPhone = "+91 98700 30055",
            nearestLandmark = "Research & Innovation Park",
            messAttached = "Scholars Mess 55"
        ),
        LpuHostel(
            code = "Block 56",
            name = "Block 56 International Residence",
            type = LpuHostelType.APARTMENTS,
            description = "Dedicated residence for international students and exchange scholars.",
            totalFloors = 7,
            wings = listOf("Wing A", "Wing B", "Wing C"),
            wardenName = "Dr. Michael D'Souza",
            wardenPhone = "+91 98123 30056",
            caretakerPhone = "+91 98700 30056",
            nearestLandmark = "Division of International Affairs",
            messAttached = "Global Continental Dining"
        )
    )

    fun findByCode(code: String): LpuHostel? {
        return allHostels.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }

    val boysHostels: List<LpuHostel> get() = allHostels.filter { it.type == LpuHostelType.BOYS }
    val girlsHostels: List<LpuHostel> get() = allHostels.filter { it.type == LpuHostelType.GIRLS }
    val apartmentHostels: List<LpuHostel> get() = allHostels.filter { it.type == LpuHostelType.APARTMENTS }

    val allCodes: List<String> get() = allHostels.map { it.code }

    // Campus Emergency Directory
    val campusEmergencyNumbers = listOf(
        Triple("Uni-Hospital 24x7 Emergency", "+91 1824 517000", "Ambulance & Campus Medical Center"),
        Triple("LPU Security Control (Gate 1 & 4)", "+91 1824 517100", "Main Security & Incident Cell"),
        Triple("DSW Hostel Administration Cell", "+91 1824 517200", "Division of Student Welfare, Block 30"),
        Triple("Estate & Maintenance Helpdesk", "+91 1824 517300", "Plumbing, Electrical, Carpentry"),
        Triple("Campus NetOps Wi-Fi NOC", "+91 1824 517400", "Hostel Internet & Network Center"),
        Triple("National Anti-Ragging Helpline", "1800 180 5522", "Toll-Free 24x7 University Monitoring")
    )
}
