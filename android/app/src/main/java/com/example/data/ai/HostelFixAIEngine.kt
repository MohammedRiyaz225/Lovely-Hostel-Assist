package com.example.data.ai

import com.example.data.model.AIAnalysis
import com.example.data.model.Complaint
import com.example.data.model.ComplaintCategory
import com.example.data.model.ComplaintPriority
import com.example.data.model.DuplicateInfo
import com.example.data.model.SentimentType
import java.util.Locale

object HostelFixAIEngine {

    private val plumbingKeywords = listOf("water", "leak", "tap", "pipe", "drain", "sink", "flush", "toilet", "geyser", "shower", "clogged", "overflow", "basin", "drip", "sewage")
    private val electricalKeywords = listOf("light", "fan", "switch", "power", "socket", "short circuit", "spark", "fuse", "ac", "wire", "voltage", "blackout", "bulb", "appliances", "tripping", "heater")
    private val cleaningKeywords = listOf("dirty", "trash", "dustbin", "sweep", "mop", "smell", "garbage", "washroom dirty", "corridor", "pest", "cockroach", "litter", "insect", "unhygienic", "stink")
    private val wifiKeywords = listOf("wifi", "wi-fi", "internet", "router", "connection", "lan", "slow speed", "disconnect", "signal", "dns", "portal", "ethernet", "network", "bandwidth", "ip")
    private val foodKeywords = listOf("mess", "food", "dinner", "lunch", "breakfast", "meal", "caterer", "uncooked", "stale", "taste", "hygiene", "roti", "curry", "water cooler", "insect in food")
    private val furnitureKeywords = listOf("bed", "chair", "table", "desk", "cupboard", "almirah", "wardrobe", "mattress", "drawer", "hinge", "door handle", "cot", "shelf")
    private val securityKeywords = listOf("theft", "stolen", "lock", "key", "stranger", "guard", "gate", "cctv", "unauthorized", "trespass", "window latch", "harassment", "lost")

    private val criticalKeywords = listOf("fire", "spark", "short circuit", "shock", "flood", "severe leak", "smoke", "theft", "emergency", "danger", "burst", "gas leak", "burning smell")
    private val highKeywords = listOf("no water", "blackout", "geyser broken", "toilet choked", "3 days", "three days", "several days", "urgent", "exam", "broken lock", "overflowing", "immediately")
    private val lowKeywords = listOf("paint", "squeak", "loose knob", "scratch", "minor", "dim light", "slow", "suggestion")

    private val negativeKeywords = listOf("terrible", "worst", "unbearable", "frustrated", "days", "pathetic", "disgusting", "nobody fixed", "again", "danger", "ruined", "urgently", "fed up", "broken", "dirty", "angry")
    private val positiveKeywords = listOf("thanks", "kindly", "please", "appreciate", "helpful", "good", "resolve soon")

    /**
     * Feature 1: AI Complaint Classification
     */
    fun classifyCategory(text: String): Pair<ComplaintCategory, Float> {
        val lower = text.lowercase(Locale.ROOT)
        val scores = mutableMapOf<ComplaintCategory, Int>()

        scores[ComplaintCategory.PLUMBING] = countMatches(lower, plumbingKeywords)
        scores[ComplaintCategory.ELECTRICAL] = countMatches(lower, electricalKeywords)
        scores[ComplaintCategory.CLEANING] = countMatches(lower, cleaningKeywords)
        scores[ComplaintCategory.WIFI] = countMatches(lower, wifiKeywords)
        scores[ComplaintCategory.FOOD] = countMatches(lower, foodKeywords)
        scores[ComplaintCategory.FURNITURE] = countMatches(lower, furnitureKeywords)
        scores[ComplaintCategory.SECURITY] = countMatches(lower, securityKeywords)

        val best = scores.maxByOrNull { it.value }
        return if (best != null && best.value > 0) {
            val confidence = (0.78f + (best.value * 0.05f).coerceAtMost(0.18f))
            Pair(best.key, confidence)
        } else {
            Pair(ComplaintCategory.GENERAL, 0.72f)
        }
    }

    /**
     * Feature 2: AI Priority Prediction
     */
    fun predictPriority(text: String, category: ComplaintCategory): ComplaintPriority {
        val lower = text.lowercase(Locale.ROOT)

        if (criticalKeywords.any { lower.contains(it) }) {
            return ComplaintPriority.CRITICAL
        }
        if (highKeywords.any { lower.contains(it) }) {
            return ComplaintPriority.HIGH
        }
        if (category == ComplaintCategory.SECURITY) {
            return ComplaintPriority.HIGH
        }
        if (lowKeywords.any { lower.contains(it) }) {
            return ComplaintPriority.LOW
        }
        return ComplaintPriority.MEDIUM
    }

    /**
     * Feature 3: AI Department Recommendation
     */
    fun recommendDepartment(category: ComplaintCategory): String {
        return when (category) {
            ComplaintCategory.PLUMBING -> "LPU Estate Maintenance (Plumbing Division)"
            ComplaintCategory.ELECTRICAL -> "LPU Electrical Power & Substations"
            ComplaintCategory.CLEANING -> "LPU Housekeeping & DSW Sanitation"
            ComplaintCategory.WIFI -> "LPU NetOps Campus IT Division"
            ComplaintCategory.FOOD -> "LPU Hostel Mess & Catering Committee"
            ComplaintCategory.FURNITURE -> "LPU Civil & Carpentry Works"
            ComplaintCategory.SECURITY -> "LPU Security & Proctorial Cell"
            ComplaintCategory.GENERAL -> "LPU General Estate Maintenance"
        }
    }

    /**
     * Feature 4: Duplicate Complaint Detection
     * Compares against other active complaints in the hostel block.
     */
    fun detectDuplicates(
        title: String,
        description: String,
        block: String,
        existingComplaints: List<Complaint>
    ): DuplicateInfo {
        val fullText = "$title $description".lowercase(Locale.ROOT)
        val tokens = tokenize(fullText)
        if (tokens.isEmpty()) return DuplicateInfo(isDuplicate = false)

        var highestSimilarity = 0.0f
        var bestMatch: Complaint? = null

        for (existing in existingComplaints) {
            // Priority check for matching block
            val blockMatches = existing.block.equals(block, ignoreCase = true)
            val existingFull = "${existing.title} ${existing.description}".lowercase(Locale.ROOT)
            val existingTokens = tokenize(existingFull)

            val jaccard = calculateJaccardSimilarity(tokens, existingTokens)
            val bonus = if (blockMatches) 0.15f else 0.0f
            val finalScore = (jaccard + bonus).coerceAtMost(0.98f)

            if (finalScore > highestSimilarity) {
                highestSimilarity = finalScore
                bestMatch = existing
            }
        }

        return if (highestSimilarity >= 0.55f && bestMatch != null) {
            DuplicateInfo(
                isDuplicate = true,
                similarComplaintId = bestMatch.id,
                similarComplaintTitle = bestMatch.title,
                similarityScore = highestSimilarity
            )
        } else {
            DuplicateInfo(isDuplicate = false, similarityScore = highestSimilarity)
        }
    }

    /**
     * Feature 5: Sentiment Analysis
     */
    fun analyzeSentiment(text: String): SentimentType {
        val lower = text.lowercase(Locale.ROOT)
        val negCount = countMatches(lower, negativeKeywords)
        val posCount = countMatches(lower, positiveKeywords)

        return when {
            negCount >= 2 || (negCount > posCount && negCount > 0) -> SentimentType.NEGATIVE
            posCount > negCount -> SentimentType.POSITIVE
            else -> SentimentType.NEUTRAL
        }
    }

    /**
     * Feature 6: AI Complaint Summarization
     */
    fun summarizeComplaint(title: String, description: String, block: String, category: ComplaintCategory): String {
        val lower = "$title $description".lowercase(Locale.ROOT)
        val locationStr = if (block.isNotBlank()) "in $block" else "in hostel"

        return when (category) {
            ComplaintCategory.PLUMBING -> {
                if (lower.contains("leak") || lower.contains("tap")) {
                    "Recurring water leakage problem reported $locationStr."
                } else if (lower.contains("geyser")) {
                    "Geyser and hot water malfunction reported $locationStr."
                } else {
                    "Plumbing and drainage issue requiring maintenance attention $locationStr."
                }
            }
            ComplaintCategory.ELECTRICAL -> {
                if (lower.contains("fan") || lower.contains("light")) {
                    "Lighting or ceiling fan fixture defect identified $locationStr."
                } else if (lower.contains("spark") || lower.contains("socket")) {
                    "Urgent electrical outlet/spark hazard flagged $locationStr."
                } else {
                    "Power supply interruption or electrical defect $locationStr."
                }
            }
            ComplaintCategory.WIFI -> "Network connectivity outage affecting residents $locationStr."
            ComplaintCategory.CLEANING -> "Sanitation cleanup and hygiene restoration required $locationStr."
            ComplaintCategory.FOOD -> "Mess hygiene or food quality concern flagged $locationStr."
            ComplaintCategory.FURNITURE -> "Damaged room furniture or woodwork item needing repair $locationStr."
            ComplaintCategory.SECURITY -> "Security concern or access latch vulnerability reported $locationStr."
            ComplaintCategory.GENERAL -> "General maintenance inspection requested $locationStr."
        }
    }

    /**
     * Full AI Analysis Pipeline
     */
    fun runFullPipeline(
        title: String,
        description: String,
        block: String,
        existingComplaints: List<Complaint> = emptyList()
    ): AIAnalysis {
        val combinedText = "$title $description"
        val (category, confidence) = classifyCategory(combinedText)
        val priority = predictPriority(combinedText, category)
        val department = recommendDepartment(category)
        val sentiment = analyzeSentiment(combinedText)
        val summary = summarizeComplaint(title, description, block, category)

        val estimatedHours = when (priority) {
            ComplaintPriority.CRITICAL -> 4
            ComplaintPriority.HIGH -> 12
            ComplaintPriority.MEDIUM -> 24
            ComplaintPriority.LOW -> 48
        }

        return AIAnalysis(
            category = category,
            priority = priority,
            department = department,
            sentiment = sentiment,
            confidence = confidence,
            summary = summary,
            estimatedResolutionHours = estimatedHours
        )
    }

    private val exactWordKeywords = setOf("ac", "ip", "cot", "tap", "key", "bed")

    private fun countMatches(text: String, keywords: List<String>): Int {
        return keywords.count { kw ->
            if (kw in exactWordKeywords) {
                Regex("\\b${Regex.escape(kw)}s?\\b").containsMatchIn(text)
            } else {
                text.contains(kw)
            }
        }
    }

    private fun tokenize(text: String): Set<String> {
        val stopWords = setOf("the", "is", "at", "which", "on", "in", "a", "an", "and", "or", "for", "to", "has", "been", "was", "my", "our", "it", "from")
        return text.split(Regex("[^a-zA-Z0-9]+"))
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.length > 2 && !stopWords.contains(it) }
            .toSet()
    }

    private fun calculateJaccardSimilarity(setA: Set<String>, setB: Set<String>): Float {
        if (setA.isEmpty() || setB.isEmpty()) return 0.0f
        val intersection = setA.intersect(setB).size
        val union = setA.union(setB).size
        return if (union == 0) 0.0f else intersection.toFloat() / union.toFloat()
    }
}
