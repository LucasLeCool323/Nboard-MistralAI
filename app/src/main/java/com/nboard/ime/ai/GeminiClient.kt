package com.nboard.ime.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiClient(private val apiKey: String) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    suspend fun generateText(
        prompt: String,
        systemInstruction: String? = null,
        outputCharLimit: Int = 0
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Mistral API key missing"))
        }
        generateWithModel(prompt, systemInstruction, outputCharLimit)
    }

    private fun generateWithModel(
        prompt: String,
        systemInstruction: String?,
        outputCharLimit: Int
    ): Result<String> {
        val url = "https://api.mistral.ai/v1/chat/completions"

        val messages = JSONArray()
        if (!systemInstruction.isNullOrBlank()) {
            messages.put(JSONObject().put("role", "system").put("content", systemInstruction.trim()))
        }
        messages.put(JSONObject().put("role", "user").put("content", prompt))

        val requestJson = JSONObject()
            .put("model", "mistral-small-latest")
            .put("messages", messages)
            .put("temperature", 0.3)
            .put("max_tokens", 256)

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val errorMessage = extractApiErrorMessage(bodyString)
                    return Result.failure(IOException(errorMessage ?: "Mistral request failed (${response.code})"))
                }
                if (bodyString.isBlank()) return Result.failure(IOException("Mistral returned an empty response"))

                val json = JSONObject(bodyString)
                var text = extractCandidateText(json)
                if (outputCharLimit > 0 && text.length > outputCharLimit) {
                    text = text.take(outputCharLimit).trimEnd().plus("…")
                }
                if (text.isNotBlank()) return Result.success(text)
                Result.failure(IOException("Mistral response had no text output"))
            }
        } catch (error: Exception) {
            Result.failure(IOException(error.message ?: "Mistral request error", error))
        }
    }

    private fun extractCandidateText(json: JSONObject): String {
        val choices = json.optJSONArray("choices") ?: return ""
        val first = choices.optJSONObject(0) ?: return ""
        return first.optJSONObject("message")?.optString("content").orEmpty().trim()
    }

    private fun extractApiErrorMessage(body: String): String? {
        if (body.isBlank()) return null
        return runCatching {
            JSONObject(body)
                .optJSONObject("error")
                ?.optString("message")
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}