package com.example.summary

import com.example.classify.EmailMeta

data class Summary(val text: String, val actions: List<String>)

interface Summarizer {
    suspend fun summarize(email: EmailMeta, category: String): Summary
}
