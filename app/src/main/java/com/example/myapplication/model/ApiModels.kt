package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

// --- OpenAI-compatible models (for /v1/models) ---

data class ModelsResponse(
    val `object`: String? = null,
    val data: List<ModelInfo> = emptyList()
)

data class ModelInfo(
    val id: String,
    val `object`: String? = null,
    val created: Long? = null,
    @SerializedName("owned_by") val ownedBy: String? = null
)

// --- OpenAI-compatible Chat Completions API (for /v1/chat/completions) ---

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double? = null,
    val max_tokens: Int? = null,
    val stream: Boolean = false
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage?
)

data class Choice(
    val index: Int,
    val message: ChatMessage,
    @SerializedName("finish_reason") val finishReason: String?
)

data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int?,
    @SerializedName("completion_tokens") val completionTokens: Int?,
    @SerializedName("total_tokens") val totalTokens: Int?
)

// --- Native v1 API (for /api/v1/chat with MCP support) ---

data class V1ChatRequest(
    val model: String,
    val input: String,
    val integrations: List<String>? = null,
    val stream: Boolean = false
)

data class V1ChatResponse(
    // Simple content fallback
    val content: String?,
    @SerializedName("stop_reason") val stopReason: String?,
    val stats: V1Stats?,
    // New LM Studio format: output[] array
    @SerializedName("output") val outputItems: List<V1OutputItem>?
)

data class V1OutputItem(
    val type: String?,          // "message", "reasoning", "tool_call", "tool_result"
    val content: String?,       // text content for message/reasoning
    val tool: String?,          // tool name for tool_call
    val output: String?,        // tool result string (JSON) for tool_call
    val error: String?          // error string for tool_result
)

data class V1Stats(
    @SerializedName("tokens_per_second") val tokensPerSecond: Double?,
    @SerializedName("total_tokens_count") val totalTokensCount: Int?
)

