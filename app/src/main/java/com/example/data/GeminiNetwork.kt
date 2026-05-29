package com.example.data

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service to connect with the Gemini API for metadata categorization and thread summaries.
 * Employs direct REST API as we are in prototype/demo mode.
 */
object GeminiNetwork {
    private const val TAG = "GeminiNetwork"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Categorizes a batch of emails. Returns a map from message ID to prediction results.
     */
    suspend fun categorizeEmails(emailsJsonArray: String): Map<String, CategorizationResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            Log.e(TAG, "Gemini API key is not configured. Returning fallback mock categorization.")
            return@withContext fallbackMockCategorization(emailsJsonArray)
        }

        val prompt = """
            You are an intelligent email analyzer. Categorize each email provided in the list into exactly ONE category: "Work", "Bill", "Newsletter", "Promotion", "Social".
            Also decide if the user should unsubscribe from it (isUnsubscribeSuggested) and if it is low-priority clutter (isClutter).
            
            Input emails list:
            $emailsJsonArray
            
            Return the results ONLY as a JSON array matching exactly this schema:
            [
              {
                "id": "message_id_here",
                "category": "Work/Bill/Newsletter/Promotion/Social",
                "isUnsubscribeSuggested": true/false,
                "isClutter": true/false
              }
            ]
            Do not write any introduction, any code block tick marks, or explanation. Format ONLY as raw valid JSON, ready for parsing.
        """.trimIndent()

        val responseText = makeGeminiPostRequest(apiKey, prompt) ?: return@withContext fallbackMockCategorization(emailsJsonArray)
        
        try {
            val resultMap = mutableMapOf<String, CategorizationResult>()
            val cleanedJson = cleanJsonResponse(responseText)
            val jsonArray = JSONArray(cleanedJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val category = obj.optString("category", "Work")
                val isUnsubscribe = obj.optBoolean("isUnsubscribeSuggested", false)
                val isClutter = obj.optBoolean("isClutter", false)
                resultMap[id] = CategorizationResult(category, isUnsubscribe, isClutter)
            }
            return@withContext resultMap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini categorization response: $e. Raw response: $responseText")
            return@withContext fallbackMockCategorization(emailsJsonArray)
        }
    }

    /**
     * Generates a thread summary and recommended next actions (reply, snooze, archv) for an email.
     */
    suspend fun generateThreadSummary(
        sender: String,
        subject: String,
        snippet: String
    ): SummaryResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            Log.e(TAG, "Gemini API Key missing. Returning mock summary.")
            return@withContext generateMockSummary(sender, subject)
        }

        val prompt = """
            You are a helpful smart email assistant. Analyze this email metadata:
            Sender: $sender
            Subject: $subject
            Snippet: $snippet
            
            1. Write a 1-to-2 sentence summary of this thread.
            2. Suggest 3 short smart client actions (like Reply, Snooze, Archive, Delete).
            
            Return ONLY as a JSON object matching this schema:
            {
              "summary": "This email contains...",
              "actions": ["Action 1", "Action 2", "Action 3"]
            }
            Do not include markdown styles, or any explanation. Return RAW valid JSON.
        """.trimIndent()

        val responseText = makeGeminiPostRequest(apiKey, prompt) ?: return@withContext generateMockSummary(sender, subject)
        
        try {
            val cleanedJson = cleanJsonResponse(responseText)
            val obj = JSONObject(cleanedJson)
            val summary = obj.getString("summary")
            val actionsArray = obj.getJSONArray("actions")
            val actions = mutableListOf<String>()
            for (i in 0 until actionsArray.length()) {
                actions.add(actionsArray.getString(i))
            }
            return@withContext SummaryResult(summary, actions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse thread summary: $e. Raw response: $responseText")
            return@withContext generateMockSummary(sender, subject)
        }
    }

    private fun cleanJsonResponse(rawResponse: String): String {
        var clean = rawResponse.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }

    private fun makeGeminiPostRequest(apiKey: String, promptText: String): String? {
        val url = "$BASE_URL?key=$apiKey"
        
        // Assemble request body matching standard Gemini API schema
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Unsuccessful response from Gemini API: code ${response.code}, body: ${response.body?.string()}")
                    return null
                }
                val bodyStr = response.body?.string() ?: return null
                
                // Parse standard Gemini response schema to extract candidate text
                val root = JSONObject(bodyStr)
                val candidates = root.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    if (parts.length() > 0) {
                        return parts.getJSONObject(0).getString("text")
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network exception calling Gemini: $e")
        } catch (e: Exception) {
            Log.e(TAG, "Parsing exception for Gemini response: $e")
        }
        return null
    }

    // --- FALLBACK MOCK COMPUTATION (For offline mode / empty API keys) ---

    private fun fallbackMockCategorization(emailsJson: String): Map<String, CategorizationResult> {
        val map = mutableMapOf<String, CategorizationResult>()
        try {
            val list = JSONArray(emailsJson)
            for (i in 0 until list.length()) {
                val email = list.getJSONObject(i)
                val id = email.getString("id")
                val subject = email.getString("subject").lowercase()
                val sender = email.getString("sender").lowercase()
                
                // Deterministic local smart heuristic rules based on keywords
                val result = when {
                    subject.contains("invoice") || subject.contains("bill") || subject.contains("payment") || subject.contains("renew") || subject.contains("receipt") -> {
                        CategorizationResult("Bill", isUnsubscribeSuggested = false, isClutter = false)
                    }
                    sender.contains("newsletter") || subject.contains("newsletter") || sender.contains("digest") || sender.contains("weekly") -> {
                        CategorizationResult("Newsletter", isUnsubscribeSuggested = true, isClutter = true)
                    }
                    subject.contains("sale") || subject.contains("off") || subject.contains("discount") || subject.contains("deal") || sender.contains("promo") || sender.contains("marketing") -> {
                        CategorizationResult("Promotion", isUnsubscribeSuggested = true, isClutter = true)
                    }
                    sender.contains("linkedin") || sender.contains("facebook") || sender.contains("twitter") || sender.contains("instagram") || sender.contains("social") || subject.contains("invite") -> {
                        CategorizationResult("Social", isUnsubscribeSuggested = true, isClutter = false)
                    }
                    else -> {
                        // Work category default if standard email keyword is matched
                        if (subject.contains("git") || subject.contains("project") || subject.contains("review") || subject.contains("meeting") || sender.contains("team") || sender.contains("manager")) {
                            CategorizationResult("Work", isUnsubscribeSuggested = false, isClutter = false)
                        } else {
                            // Split evenly to populate a lively UI
                            val dynamicCat = when (i % 5) {
                                0 -> "Work"
                                1 -> "Bill"
                                2 -> "Newsletter"
                                3 -> "Promotion"
                                else -> "Social"
                            }
                            val devUnsubscribe = (dynamicCat == "Newsletter" || dynamicCat == "Promotion")
                            val devClutter = devUnsubscribe || (i % 2 == 0 && dynamicCat == "Social")
                            CategorizationResult(dynamicCat, isUnsubscribeSuggested = devUnsubscribe, isClutter = devClutter)
                        }
                    }
                }
                map[id] = result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in local categorization simulation: $e")
        }
        return map
    }

    private fun generateMockSummary(sender: String, subject: String): SummaryResult {
        val cleanSender = sender.split("<")[0].trim()
        val summary = "A thread from $cleanSender regarding '$subject'. It asks for regular status updates or contains promotional materials that are under review."
        val actions = listOf("Reply directly", "Snooze 1 day", "Archive instantly")
        return SummaryResult(summary, actions)
    }

    data class CategorizationResult(
        val category: String,
        val isUnsubscribeSuggested: Boolean,
        val isClutter: Boolean
    )

    data class SummaryResult(
        val summary: String,
        val actions: List<String>
    )
}
