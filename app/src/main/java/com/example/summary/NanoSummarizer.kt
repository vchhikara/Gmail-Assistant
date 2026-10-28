package com.example.summary

import android.content.Context
import android.util.Log
import com.example.classify.EmailMeta
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.SummarizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Gemini Nano summaries through ML Kit GenAI (AICore). Used only when the model is already on the device;
 * every other case, including errors, goes to [fallback] so the app behaves the same on all phones.
 */
class NanoSummarizer(context: Context, private val fallback: Summarizer) : Summarizer {
    private val client by lazy {
        Summarization.getClient(
            SummarizerOptions.builder(context.applicationContext)
                .setInputType(SummarizerOptions.InputType.CONVERSATION)
                .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
                .setLanguage(SummarizerOptions.Language.ENGLISH)
                .build()
        )
    }

    override suspend fun summarize(email: EmailMeta, category: String): Summary {
        val base = fallback.summarize(email, category)
        val text = "${email.sender}: ${email.subject}. ${email.snippet}"
        if (text.length < MIN_INPUT || android.os.Build.VERSION.SDK_INT < 26) return base
        return try {
            if (client.checkFeatureStatus().await() != FeatureStatus.AVAILABLE) return base
            val result = client.runInference(SummarizationRequest.builder(text).build()).await()
            val summary = result.summary.trim().removePrefix("-").removePrefix("•").trim()
            if (summary.isEmpty()) base else base.copy(text = summary)
        } catch (e: Exception) {
            Log.w("NanoSummarizer", "Nano unavailable, using rules: ${e.message}")
            base
        }
    }

    private suspend fun <T> ListenableFuture<T>.await(): T = suspendCancellableCoroutine { cont ->
        addListener({
            try { cont.resume(get()) } catch (e: Exception) { cont.resumeWithException(e.cause ?: e) }
        }, { it.run() })
        cont.invokeOnCancellation { cancel(true) }
    }

    private companion object { const val MIN_INPUT = 120 }
}
