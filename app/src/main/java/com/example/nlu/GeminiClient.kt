package com.example.nlu

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val tools: List<JsonObject>? = null,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String? = null, val functionCall: JsonObject? = null)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val responseMimeType: String? = null
)

@Serializable
data class GenerateContentResponse(val candidates: List<Candidate>)

@Serializable
data class Candidate(val content: Content)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
        
    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiClient {
    
    private val systemInstructions = """
        You are Jarvis, a voice-controlled assistant embedded in an Android app. Follow these rules strictly:
        - Return valid JSON matching the function-call schema provided.
        - If no function applies, use get_info() or a plain "reply" field.
        - Keep spoken/TTS replies under 20 words.
        - For irreversible actions, include "requires_confirmation": true.
        - If ambiguous, return a clarify field.
        - Never return a function call for reading/storing PIN. Device unlock is biometric only.
    """.trimIndent()
    
    private val toolSchema = Json.parseToJsonElement("""
        {
          "functionDeclarations": [
            {
              "name": "open_app",
              "description": "Open an application",
              "parameters": {
                "type": "OBJECT",
                "properties": {
                  "app_name": { "type": "STRING" }
                },
                "required": ["app_name"]
              }
            },
            {
              "name": "send_message",
              "description": "Send a message",
              "parameters": {
                "type": "OBJECT",
                "properties": {
                  "contact": { "type": "STRING" },
                  "message": { "type": "STRING" },
                  "channel": {
                    "type": "STRING",
                    "enum": ["sms", "whatsapp"]
                  }
                },
                "required": ["contact", "message", "channel"]
              }
            },
            {
              "name": "toggle_setting",
              "description": "Toggle a system setting",
              "parameters": {
                "type": "OBJECT",
                "properties": {
                  "setting": {
                    "type": "STRING",
                    "enum": ["wifi", "bluetooth", "dnd", "flashlight"]
                  },
                  "state": {
                    "type": "STRING",
                    "enum": ["on", "off"]
                  }
                },
                "required": ["setting", "state"]
              }
            },
            {
              "name": "set_volume",
              "description": "Set media volume",
              "parameters": {
                "type": "OBJECT",
                "properties": {
                  "level": {
                    "type": "INTEGER",
                    "description": "0-100"
                  }
                },
                "required": ["level"]
              }
            },
            {
              "name": "read_notifications",
              "description": "Read recent notifications",
              "parameters": {
                "type": "OBJECT",
                "properties": {
                  "app_filter": { "type": "STRING" }
                }
              }
            },
            {
              "name": "unlock_device",
              "description": "Unlock the device using BiometricPrompt"
            },
            {
              "name": "get_info",
              "description": "General Q&A or info lookup",
              "parameters": {
                "type": "OBJECT",
                "properties": {
                  "query": { "type": "STRING" },
                  "reply": { "type": "STRING" }
                },
                "required": ["query", "reply"]
              }
            },
            {
              "name": "reply_only",
              "description": "Just reply to the user without action",
              "parameters": {
                "type": "OBJECT",
                "properties": {
                  "reply": { "type": "STRING" },
                  "clarify": { "type": "STRING" }
                }
              }
            }
          ]
        }
    """).jsonObject

    suspend fun parseIntent(apiKey: String, transcript: String): JsonObject? = withContext(Dispatchers.IO) {
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = transcript)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructions))),
            tools = listOf(toolSchema),
            generationConfig = GenerationConfig(temperature = 0.2f)
        )
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val firstPart = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()
            // Check if it's a function call
            firstPart?.functionCall ?: buildJsonObject { 
                put("name", "reply_only")
                putJsonObject("args") {
                    put("reply", firstPart?.text ?: "I didn't understand.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
