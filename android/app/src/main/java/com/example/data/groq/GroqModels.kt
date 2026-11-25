package com.example.data.groq

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val groundingSources: List<GroundingSource> = emptyList(),
    val isLoading: Boolean = false
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class GroundingSource(
    val title: String,
    val url: String
)

enum class GroqModel(val modelId: String, val displayName: String, val description: String) {
    LLAMA_3_3_70B(
        modelId = "llama-3.3-70b-versatile",
        displayName = "Llama 3.3 70B",
        description = "Balanced reasoning for LPU hostel & maintenance help"
    ),
    LLAMA_3_1_8B(
        modelId = "llama-3.1-8b-instant",
        displayName = "Llama 3.1 8B Instant",
        description = "Ultra-fast responses with high rate limits"
    ),
    GPT_OSS_120B(
        modelId = "openai/gpt-oss-120b",
        displayName = "GPT-OSS 120B",
        description = "Deep diagnostics & complex facility troubleshooting"
    ),
    GPT_OSS_20B(
        modelId = "openai/gpt-oss-20b",
        displayName = "GPT-OSS 20B",
        description = "Fast reasoning for quick campus queries"
    )
}

enum class AssistantMode(val displayName: String, val instructionHint: String) {
    CAMPUS_GUIDE(
        displayName = "LPU Campus Guide",
        instructionHint = "Focus on LPU hostel blocks, wardens, campus landmarks, and HostelFix ticket submission."
    ),
    SAFETY_TRIAGE(
        displayName = "Safety & Triage",
        instructionHint = "Prioritize immediate electrical, plumbing, and fire safety steps plus complaint priority advice."
    ),
    GENERAL(
        displayName = "General Assistant",
        instructionHint = "Provide clear, concise troubleshooting and resident support."
    )
}
