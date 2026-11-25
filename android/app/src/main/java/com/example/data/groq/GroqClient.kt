package com.example.data.groq

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GroqClient {

    private const val TAG = "GroqClient"
    private const val BASE_URL = "https://api.groq.com/openai/v1/chat/completions"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_INSTRUCTION = """
You are the official HostelFix AI Campus & Facility Assistant for Lovely Professional University (LPU), Phagwara, Punjab.
Your campus expertise covers:
1. All 18+ LPU Residential Blocks:
   - Boys Hostels: BH-1, BH-2, BH-3, BH-4, BH-5, BH-6, BH-7, BH-8
   - Girls Hostels: GH-1, GH-2, GH-3, GH-4, GH-5, GH-6
   - Apartment Towers: Uni-Living (Studio & 1BHK/2BHK), Block 34, Block 36, Block 38, Block 55 (Postgraduates), Block 56 (International Students)
2. Campus Landmarks & Amenities:
   - Uni-Mall: Central shopping hub, tech/electronics repair shops, pharmacy, salons, and stationery.
   - Uni-Hospital: 24x7 University Hospital & Emergency (+91 1824 517000) opposite GH-1 & Main Lawn.
   - Division of Student Welfare (DSW): Block 30, Room 302 (+91 1824 517200) for student grievances and welfare.
   - Central Messes (Mess 1 to 8, Girls Mess 1 to 6, Uni-Living Dining).
   - Estate & Maintenance Division: Block 29/30 ground floor (+91 1824 517300) for plumbing, electrical, civil, and carpentry.
   - Campus NetOps IT / Wi-Fi: Block 29 NOC (+91 1824 517400).
   - Gates & Security: Main Gate 1 (GT Road), Gate 2, Gate 3, Gate 4 (+91 1824 517100).
3. Student Safety & Troubleshooting:
   - Provide immediate safety instructions for water leaks, electrical spark hazards, geyser issues, and broken locks.
   - Advise how to submit complaints through HostelFix with automated category routing.
Keep your responses helpful, clear, polite, and well-structured using bullet points.
"""

    data class GroqReply(
        val text: String,
        val modelUsed: String,
        val sources: List<GroundingSource> = emptyList()
    )

    internal fun buildAttemptPlan(primaryModel: GroqModel): List<String> {
        val plan = mutableListOf(primaryModel.modelId)
        val fallbackModelIds = listOf(
            GroqModel.LLAMA_3_1_8B.modelId,
            GroqModel.LLAMA_3_3_70B.modelId,
            GroqModel.GPT_OSS_20B.modelId,
            GroqModel.GPT_OSS_120B.modelId
        )
        for (fallbackId in fallbackModelIds) {
            if (!fallbackId.equals(primaryModel.modelId, ignoreCase = true)) {
                plan.add(fallbackId)
            }
        }
        return plan.distinct()
    }

    internal fun buildRequestJson(
        history: List<ChatMessage>,
        userMessage: String,
        modelId: String,
        mode: AssistantMode = AssistantMode.CAMPUS_GUIDE
    ): JSONObject {
        val rootJson = JSONObject()
        rootJson.put("model", modelId)
        rootJson.put("temperature", 0.4)
        rootJson.put("max_completion_tokens", 1024)

        val messagesArray = JSONArray()
        messagesArray.put(
            JSONObject()
                .put("role", "system")
                .put("content", "$SYSTEM_INSTRUCTION\nActive Focus Mode: ${mode.instructionHint}")
        )

        val trimmedUserMessage = userMessage.trim()

        // Filter out synthetic greeting bubbles, loading placeholders, and empty messages
        val sanitizedHistory = history.filter { msg ->
            !msg.isLoading &&
                !msg.id.startsWith("welcome_") &&
                msg.role != MessageRole.SYSTEM &&
                msg.text.isNotBlank()
        }.toMutableList()

        // Avoid duplicating the current user message if the caller already appended it to history
        if (sanitizedHistory.lastOrNull()?.let {
                it.role == MessageRole.USER && it.text.trim() == trimmedUserMessage
            } == true
        ) {
            sanitizedHistory.removeAt(sanitizedHistory.lastIndex)
        }

        // Keep alternating user/assistant turns starting with a user turn
        val recentHistory = sanitizedHistory.takeLast(10).dropWhile { it.role != MessageRole.USER }
        var lastRole: String? = null
        for (msg in recentHistory) {
            val roleStr = if (msg.role == MessageRole.USER) "user" else "assistant"
            if (roleStr == lastRole) continue
            messagesArray.put(
                JSONObject()
                    .put("role", roleStr)
                    .put("content", msg.text)
            )
            lastRole = roleStr
        }

        if (lastRole == "user" && messagesArray.length() > 1) {
            messagesArray.remove(messagesArray.length() - 1)
        }

        messagesArray.put(
            JSONObject()
                .put("role", "user")
                .put("content", trimmedUserMessage)
        )

        rootJson.put("messages", messagesArray)
        return rootJson
    }

    private fun displayNameForModelId(modelId: String): String {
        return GroqModel.entries.firstOrNull { it.modelId.equals(modelId, ignoreCase = true) }?.displayName
            ?: modelId
    }

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        model: GroqModel = GroqModel.LLAMA_3_3_70B,
        mode: AssistantMode = AssistantMode.CAMPUS_GUIDE
    ): GroqReply = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GROQ_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GROQ_API_KEY") {
            val fallbackResponse = getLocalAssistantFallback(userMessage)
            return@withContext GroqReply(
                text = fallbackResponse + "\n\n💡 *Note: To enable live Groq AI models, add your `GROQ_API_KEY` in the AI Studio Secrets panel.*",
                modelUsed = "LPU Campus Guide (Offline)"
            )
        }

        val attempts = buildAttemptPlan(model)
        var lastHttpCode = 0
        var lastErrorWasQuota = false

        for ((index, modelId) in attempts.withIndex()) {
            try {
                if (index > 0 && lastHttpCode == 429) {
                    delay(400L)
                }

                val rootJson = buildRequestJson(history, userMessage, modelId, mode)
                val requestBody = rootJson.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url(BASE_URL)
                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        lastHttpCode = response.code
                        if (response.code == 429 ||
                            bodyStr.contains("rate_limit", ignoreCase = true) ||
                            bodyStr.contains("insufficient_quota", ignoreCase = true)
                        ) {
                            lastErrorWasQuota = true
                        }
                        Log.w(TAG, "Groq attempt $modelId returned HTTP ${response.code}; trying fallback")

                        if (response.code == 401 || response.code == 403) {
                            return@withContext GroqReply(
                                text = "Live Groq authentication requires a valid GROQ_API_KEY (HTTP ${response.code}). Showing built-in LPU campus guidance:\n\n" +
                                    getLocalAssistantFallback(userMessage),
                                modelUsed = "LPU Campus Guide"
                            )
                        }
                        return@use
                    }

                    val responseJson = JSONObject(bodyStr)
                    val choices = responseJson.optJSONArray("choices")
                    if (choices == null || choices.length() == 0) {
                        return@use
                    }

                    val firstChoice = choices.getJSONObject(0)
                    val messageObj = firstChoice.optJSONObject("message")
                    val replyText = messageObj?.optString("content").orEmpty().trim().ifBlank {
                        "No response text received from Groq model."
                    }

                    return@withContext GroqReply(
                        text = replyText,
                        modelUsed = displayNameForModelId(modelId)
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                Log.w(TAG, "Groq network unavailable on $modelId: ${e.localizedMessage}")
                break
            } catch (e: Exception) {
                Log.w(TAG, "Groq request failed on $modelId: ${e.localizedMessage}")
            }
        }

        val fallbackBody = getLocalAssistantFallback(userMessage)
        val statusNotice = when {
            lastErrorWasQuota || lastHttpCode == 429 ->
                "\n\nℹ️ *Note: Live Groq rate limit is currently reached on this API key, so I answered using the built-in LPU Campus Facility Guide.*"
            lastHttpCode > 0 ->
                "\n\nℹ️ *Note: Live Groq service is temporarily unavailable (HTTP $lastHttpCode), so I answered using the built-in LPU Campus Facility Guide.*"
            else ->
                "\n\nℹ️ *Note: Offline mode active — answered using the built-in LPU Campus Facility Guide.*"
        }
        return@withContext GroqReply(
            text = fallbackBody + statusNotice,
            modelUsed = "LPU Campus Guide"
        )
    }

    private fun getLocalAssistantFallback(userMessage: String): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("water") || lower.contains("leak") || lower.contains("plumb") -> {
                "🚰 **Plumbing Guidance for LPU Hostel Residents**\n" +
                    "• **Immediate Action**: Turn off the angle valve under the basin or the main stop-cock for your wing (Wing A/B/C) to prevent floor flooding.\n" +
                    "• **Safety**: Keep all power adapters and extension cords elevated off wet surfaces.\n" +
                    "• **HostelFix Filing**: Report under **Plumbing** category. Our AI will automatically assign it to LPU Estate Maintenance (Plumbing Division).\n" +
                    "• **LPU Estate Control Room**: Contact +91 1824 517300 or your hostel block duty warden if water is leaking into corridors."
            }
            lower.contains("spark") || lower.contains("light") || lower.contains("fan") || lower.contains("electric") || lower.contains("geyser") -> {
                "⚡ **Electrical Safety & Resolution Steps (LPU)**\n" +
                    "• **Urgent Precaution**: Do NOT touch smoking or sparking switches/geysers. Switch off the main MCB breaker on your floor panel if accessible.\n" +
                    "• **Reporting**: Submit a ticket with **Electrical** category. Any spark or burning smell is automatically prioritized as **Critical**.\n" +
                    "• **LPU Electrical Response**: LPU Electrical Substation team is stationed at Block 29/30 with emergency night support."
            }
            lower.contains("wifi") || lower.contains("internet") || lower.contains("network") -> {
                "📶 **LPU Campus Wi-Fi Troubleshooting**\n" +
                    "• **SSID Details**: Connect to `LPU_Hostel_HighSpeed` or `LPU_Campus_5G`.\n" +
                    "• **Portal Login**: Visit `http://10.10.0.1` or `internet.lpu.in` in your browser if captive portal login does not appear automatically.\n" +
                    "• **Reporting**: If entire wing (e.g. BH-4 Wing B or GH-2 Wing C) is down, report under **Wi-Fi & Network**. LPU NetOps NOC (+91 1824 517400) typically resets corridor access points within 45 minutes."
            }
            lower.contains("food") || lower.contains("mess") || lower.contains("dinner") -> {
                "🍽️ **LPU Hostel Mess & Dining Information**\n" +
                    "• **Mess Timings**: Breakfast: 7:30–9:30 AM | Lunch: 12:30–2:30 PM | Dinner: 7:30–9:30 PM.\n" +
                    "• **Locations**: Central Boys Messes (BH-1 to BH-8), Girls Messes (GH-1 to GH-6), and Uni-Living Dining Court.\n" +
                    "• **Complaints**: Log concerns under **Food & Mess** category to alert the Student Mess Committee and DSW Food Inspector."
            }
            lower.contains("shop") || lower.contains("where") || lower.contains("store") || lower.contains("pharmacy") || lower.contains("medical") || lower.contains("mall") -> {
                "📍 **LPU Campus Facilities & Nearby Amenities**\n" +
                    "• **Uni-Hospital**: 24x7 University Hospital & Ambulance opposite GH-1 & Main Lawn (+91 1824 517000).\n" +
                    "• **Uni-Mall**: Central student shopping mall — contains electronics & laptop repair shops, hardware tools, pharmacy, salon, and stationery.\n" +
                    "• **DSW Office**: Division of Student Welfare located at Block 30, Room 302 (+91 1824 517200).\n" +
                    "• **Security Control Room**: Main Gate 1 & Gate 4 (+91 1824 517100)."
            }
            lower.contains("warden") || lower.contains("contact") || lower.contains("phone") || lower.contains("caretaker") -> {
                "📞 **LPU Hostel Warden & Emergency Contacts**\n" +
                    "• **Boys Hostels (BH-1 to BH-8)**: Wardens available at respective hostel ground floor offices (e.g., BH-4 Warden: +91 98123 10004).\n" +
                    "• **Girls Hostels (GH-1 to GH-6)**: Duty Wardens available 24x7 at block security entrance (e.g., GH-2 Warden: +91 98123 20002).\n" +
                    "• **Uni-Living Apartments**: Caretaker & Warden Desk: +91 98123 30001.\n" +
                    "• **Campus 24x7 Emergency**: Uni-Hospital Ambulance (+91 1824 517000) | DSW Hostel Cell (+91 1824 517200)."
            }
            else -> {
                "🤖 **HostelFix LPU Campus AI Assistant**\n" +
                    "I am your official Lovely Professional University hostel facility & campus guide.\n" +
                    "• You can ask me how to file maintenance tickets for any of the 18+ LPU Hostel blocks (BH-1..8, GH-1..6, Uni-Living).\n" +
                    "• Ask for duty warden numbers, campus helplines, or directions to Uni-Mall, Uni-Hospital, and DSW.\n" +
                    "• Powered by Groq fast inference with built-in offline LPU facility guidance."
            }
        }
    }
}
