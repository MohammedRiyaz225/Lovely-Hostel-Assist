package com.example

import com.example.data.ai.HostelFixAIEngine
import com.example.data.model.AIAnalysis
import com.example.data.model.Complaint
import com.example.data.model.ComplaintCategory
import com.example.data.model.ComplaintPriority
import com.example.data.model.ComplaintStatus
import com.example.data.model.SentimentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HostelFixAIEngineTest {

    @Test
    fun testAIClassificationPlumbing() {
        val (category, confidence) = HostelFixAIEngine.classifyCategory("Water has been leaking from the washroom tap for two days")
        assertEquals(ComplaintCategory.PLUMBING, category)
        assertTrue(confidence > 0.70f)
    }

    @Test
    fun testAIClassificationElectrical() {
        val (category, _) = HostelFixAIEngine.classifyCategory("Ceiling fan is making loud screeching noise and spark in socket")
        assertEquals(ComplaintCategory.ELECTRICAL, category)
    }

    @Test
    fun testAIClassificationWifi() {
        val (category, _) = HostelFixAIEngine.classifyCategory("Wi-Fi router keeps disconnecting on 3rd floor wing")
        assertEquals(ComplaintCategory.WIFI, category)
    }

    @Test
    fun testAIPriorityPredictionCritical() {
        val priority = HostelFixAIEngine.predictPriority("Short circuit and spark with burning smell from socket", ComplaintCategory.ELECTRICAL)
        assertEquals(ComplaintPriority.CRITICAL, priority)
    }

    @Test
    fun testAIDepartmentRouting() {
        val dept = HostelFixAIEngine.recommendDepartment(ComplaintCategory.PLUMBING)
        assertTrue(dept.contains("Plumbing"))
    }

    @Test
    fun testDuplicateDetection() {
        val existing = listOf(
            Complaint(
                id = "CMP-1021",
                title = "Water leakage in washroom tap",
                description = "Water leaking continuously from tap #3 in Block 34 2nd floor washroom.",
                block = "Block 34",
                roomNumber = "2F",
                status = ComplaintStatus.PENDING,
                aiAnalysis = AIAnalysis(
                    category = ComplaintCategory.PLUMBING,
                    priority = ComplaintPriority.HIGH,
                    department = "Plumbing",
                    sentiment = SentimentType.NEGATIVE,
                    confidence = 0.9f,
                    summary = "Leakage in Block 34"
                )
            )
        )

        val duplicateResult = HostelFixAIEngine.detectDuplicates(
            title = "Water leaking tap in washroom",
            description = "Washroom tap water is continuously leaking on 2nd floor in Block 34.",
            block = "Block 34",
            existingComplaints = existing
        )

        assertTrue(duplicateResult.isDuplicate)
        assertEquals("CMP-1021", duplicateResult.similarComplaintId)
        assertTrue(duplicateResult.similarityScore > 0.50f)
    }

    @Test
    fun testSentimentAnalysisNegative() {
        val sentiment = HostelFixAIEngine.analyzeSentiment("This is terrible, water has been leaking for days and nobody fixed it!")
        assertEquals(SentimentType.NEGATIVE, sentiment)
    }

    @Test
    fun testShortKeywordWordBoundaryNoFalseMatches() {
        val (chairCat, _) = HostelFixAIEngine.classifyCategory("Unable to contact office about broken chair and study desk access")
        assertEquals(ComplaintCategory.FURNITURE, chairCat)

        val (generalCat, _) = HostelFixAIEngine.classifyCategory("Need packing tape and notice board pin")
        assertEquals(ComplaintCategory.GENERAL, generalCat)
    }

    @Test
    fun testGroqAttemptPlanIncludesPrimaryAndFallbackModels() {
        val plan = com.example.data.groq.GroqClient.buildAttemptPlan(
            primaryModel = com.example.data.groq.GroqModel.LLAMA_3_3_70B
        )
        assertEquals("llama-3.3-70b-versatile", plan[0])
        assertTrue(plan.contains("llama-3.1-8b-instant"))
        assertTrue(plan.contains("openai/gpt-oss-20b"))
        assertTrue(plan.contains("openai/gpt-oss-120b"))
        assertEquals(plan.size, plan.distinct().size)
    }
}
