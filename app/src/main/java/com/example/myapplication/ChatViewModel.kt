package com.example.myapplication

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.PreferencesRepository
import com.example.myapplication.model.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.HttpException

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesRepository(application)
    private val TAG = "ChatViewModel"

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lmStudioIp = MutableStateFlow("")
    val lmStudioIp: StateFlow<String> = _lmStudioIp.asStateFlow()

    private val _lmStudioPort = MutableStateFlow("")
    val lmStudioPort: StateFlow<String> = _lmStudioPort.asStateFlow()

    private val _models = MutableStateFlow<List<ModelInfo>>(emptyList())
    val models: StateFlow<List<ModelInfo>> = _models.asStateFlow()

    private val _selectedModel = MutableStateFlow("")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _systemPrompt = MutableStateFlow("")
    val systemPrompt: StateFlow<String> = _systemPrompt.asStateFlow()

    private val _storedPrompts = MutableStateFlow<List<StoredPrompt>>(emptyList())
    @Suppress("unused")
    val storedPrompts: StateFlow<List<StoredPrompt>> = _storedPrompts.asStateFlow()

    private val _isModelLoading = MutableStateFlow(false)
    val isModelLoading: StateFlow<Boolean> = _isModelLoading.asStateFlow()

    private val _apiToken = MutableStateFlow("")
    val apiToken: StateFlow<String> = _apiToken.asStateFlow()

    private val _useApiToken = MutableStateFlow(false)
    val useApiToken: StateFlow<Boolean> = _useApiToken.asStateFlow()

    private val _authRequired = MutableStateFlow<Boolean?>(null)
    val authRequired: StateFlow<Boolean?> = _authRequired.asStateFlow()

    private val _mcpServers = MutableStateFlow("")
    val mcpServers: StateFlow<String> = _mcpServers.asStateFlow()

    // MCP Server toggle states
    private val _imageGenEnabled = MutableStateFlow(false)
    val imageGenEnabled: StateFlow<Boolean> = _imageGenEnabled.asStateFlow()

    private val _filesystemEnabled = MutableStateFlow(false)
    val filesystemEnabled: StateFlow<Boolean> = _filesystemEnabled.asStateFlow()

    private val _webSearchEnabled = MutableStateFlow(false)
    val webSearchEnabled: StateFlow<Boolean> = _webSearchEnabled.asStateFlow()

    private val _cliMcpServerEnabled = MutableStateFlow(false)
    val cliMcpServerEnabled: StateFlow<Boolean> = _cliMcpServerEnabled.asStateFlow()

    private val _blenderEnabled = MutableStateFlow(false)
    val blenderEnabled: StateFlow<Boolean> = _blenderEnabled.asStateFlow()

    private val _datetimeEnabled = MutableStateFlow(false)
    val datetimeEnabled: StateFlow<Boolean> = _datetimeEnabled.asStateFlow()

    private val _speakResponses = MutableStateFlow(false)
    val speakResponses: StateFlow<Boolean> = _speakResponses.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var voiceAgent: VoiceAgent? = null

    init {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                voiceAgent = VoiceAgent(application)
                observeSpokenText()
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to initialize VoiceAgent", t)
                _events.emit("Sprachfunktionen nicht verfügbar.")
            }
        }
        viewModelScope.launch {
            restorePreferences()

            if (_lmStudioIp.value.isNotBlank()) {
                checkAuthRequiredSuspend()
            }
        }
    }

    private suspend fun restorePreferences() {
        _lmStudioIp.value    = prefs.ipFlow.first().trim()
        _lmStudioPort.value  = prefs.portFlow.first().trim()
        _selectedModel.value = prefs.modelFlow.first()
        _systemPrompt.value  = prefs.systemPromptFlow.first()
        _apiToken.value      = prefs.apiTokenFlow.first().trim()
        _useApiToken.value   = prefs.useApiTokenFlow.first()
        _mcpServers.value    = prefs.mcpServersFlow.first()
        _storedPrompts.value = prefs.savedPromptsFlow.first()
        _imageGenEnabled.value     = prefs.imageGenEnabledFlow.first()
        _filesystemEnabled.value   = prefs.filesystemEnabledFlow.first()
        _webSearchEnabled.value    = prefs.webSearchEnabledFlow.first()
        _cliMcpServerEnabled.value = prefs.cliMcpServerEnabledFlow.first()
        _blenderEnabled.value      = prefs.blenderEnabledFlow.first()
        _datetimeEnabled.value     = prefs.datetimeEnabledFlow.first()
        _speakResponses.value      = prefs.speakResponsesFlow.first()
    }

    private fun observeSpokenText() {
        viewModelScope.launch {
            voiceAgent?.spokenText?.filter { it.isNotBlank() && !it.startsWith("Erkennungsfehler") }?.collect(::sendMessage)
        }
    }

    private fun postEvent(event: String) {
        viewModelScope.launch { _events.emit(event) }
    }

    private fun makeBaseUrl(): String? {
        val rawHost = _lmStudioIp.value.trim()
        val rawPort = _lmStudioPort.value.trim()
        if (rawHost.isBlank()) { postEvent("IP/Host darf nicht leer sein."); return null }
        if (rawHost.startsWith("http")) {
            val httpUrl = rawHost.toHttpUrlOrNull() ?: run { postEvent("Ungültige URL."); return null }
            val port = rawPort.toIntOrNull() ?: httpUrl.port
            return httpUrl.newBuilder().port(port).encodedPath("/").build().toString()
        }
        val port = rawPort.toIntOrNull() ?: 1234
        return "http://$rawHost:$port/"
    }

    private fun getApiService(): LmStudioApiService? {
        return try {
            val baseUrl = makeBaseUrl() ?: return null
            val effectiveToken = if (_useApiToken.value) _apiToken.value.trim().ifBlank { null } else null
            Log.d(TAG, "getApiService: useApiToken=${_useApiToken.value}, effectiveToken_set=${effectiveToken != null}")
            RetrofitClient.getClient(baseUrl, effectiveToken).create(LmStudioApiService::class.java)
        } catch (t: Throwable) {
            postEvent("Client-Fehler: ${t.message}")
            Log.e(TAG, "getApiService failed", t)
            null
        }
    }

    private fun buildMcpIntegrations(userText: String? = null): List<String>? {
        val enabledServers = mutableListOf<String>()
        val shouldEnableImageGen = userText?.let(::isImageIntent) ?: true
        if (_imageGenEnabled.value && shouldEnableImageGen) enabledServers.add("mcp/image-gen")
        if (_filesystemEnabled.value) enabledServers.add("mcp/filesystem")
        if (_webSearchEnabled.value) enabledServers.add("mcp/web-search")
        if (_cliMcpServerEnabled.value) enabledServers.add("mcp/terminal")
        if (_blenderEnabled.value) enabledServers.add("mcp/blender")
        if (_datetimeEnabled.value) enabledServers.add("mcp/datetime")
        val manualServers = _mcpServers.value.split(',').map { it.trim() }.filter { it.isNotBlank() }.map { if (it.startsWith("mcp/")) it else "mcp/$it" }
        enabledServers.addAll(manualServers)
        return enabledServers.distinct().ifEmpty { null }
    }

    private fun isImageIntent(userText: String): Boolean {
        val normalized = userText.lowercase()
        return listOf("bild", "image", "foto", "grafik", "illustration", "render", "zeichne", "zeichnen", "generiere", "erstelle ein bild", "create an image", "male", "draw").any { normalized.contains(it) }
    }

    private fun findImageUrl(element: JsonElement?): String? {
        if (element == null || element.isJsonNull) return null
        if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
            val value = element.asString
            if (value.startsWith("data:image/")) return value
            if (value.startsWith("http") && value.matches(Regex(".*\\.(png|jpg|jpeg|webp|gif)(\\?.*)?$", RegexOption.IGNORE_CASE))) return value
            if (value.trimStart().startsWith("[") || value.trimStart().startsWith("{")) {
                try { @Suppress("DEPRECATION") val parsed = JsonParser().parse(value); val found = findImageUrl(parsed); if (found != null) return found } catch (_: Exception) {}
            }
            return null
        }
        if (element.isJsonArray) { for (child in element.asJsonArray) { val found = findImageUrl(child); if (found != null) return found } }
        if (element.isJsonObject) {
            val obj = element.asJsonObject
            val prioritizedKeys = listOf("image_data", "url", "image", "image_url", "content", "output", "data")
            for (key in prioritizedKeys) { if (obj.has(key)) { val found = findImageUrl(obj.get(key)); if (found != null) return found } }
            for ((key, value) in obj.entrySet()) { if (key in prioritizedKeys) continue; val found = findImageUrl(value); if (found != null) return found }
        }
        return null
    }

    private fun buildV1Input(userText: String): String {
        return buildString {
            if (_systemPrompt.value.isNotBlank()) appendLine("[System]\n${_systemPrompt.value}\n")
            _messages.value.takeLast(10).forEach { appendLine("${if (it.isUser) "User:" else "Assistant:"} ${it.text}") }
            appendLine(); append("User: $userText")
        }
    }

    private fun buildChatMessages(userText: String): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()
        if (_systemPrompt.value.isNotBlank()) result.add(ChatMessage(role = "system", content = _systemPrompt.value))
        _messages.value.takeLast(10).forEach { result.add(ChatMessage(role = if (it.isUser) "user" else "assistant", content = it.text)) }
        result.add(ChatMessage(role = "user", content = userText))
        return result
    }

    fun sendMessage(text: String) {
        if (_selectedModel.value.isBlank()) { postEvent("Kein Modell ausgewählt."); return }
        val gson = Gson()
        val userMessage = Message(text = text, isUser = true)
        _messages.value += userMessage
        _isLoading.value = true
        val assistantMessage = Message(text = "", isUser = false)
        _messages.value += assistantMessage

        viewModelScope.launch {
            try {
                val apiService = getApiService() ?: run {
                    _messages.update { msgs -> msgs.map { if (it.id == assistantMessage.id) it.copy(text = "Fehler: Server nicht erreichbar.") else it } }
                    _isLoading.value = false; return@launch
                }
                val mcpIntegrations = buildMcpIntegrations(text)
                if (mcpIntegrations != null) {
                    val request = V1ChatRequest(model = _selectedModel.value, input = buildV1Input(text), integrations = mcpIntegrations, stream = false)
                    val response = withContext(Dispatchers.IO) { apiService.v1Chat(request) }
                    var resultText = ""; var resultImageUrl: String? = null; var resultError: String? = null
                    val items = response.outputItems
                    if (!items.isNullOrEmpty()) {
                        for (item in items) {
                            when (item.type) {
                                "message", "reasoning" -> { val t = item.content ?: ""; if (t.isNotBlank()) resultText += t }
                                "tool_call", "tool_result" -> {
                                    val raw = item.output
                                    if (raw != null) { @Suppress("DEPRECATION") val imgUrl = try { findImageUrl(JsonParser().parse(raw)) } catch (_: Exception) { null }; if (imgUrl != null) resultImageUrl = imgUrl }
                                    if (item.type == "tool_result" && item.error != null) resultError = item.error
                                }
                            }
                        }
                    } else { resultText = response.content ?: "" }
                    _messages.update { msgs -> msgs.map { msg -> if (msg.id != assistantMessage.id) return@map msg; var updated = msg; if (resultText.isNotBlank()) updated = updated.copy(text = resultText.trimStart('\n', '\r')); if (resultImageUrl != null) updated = updated.copy(imageUrl = resultImageUrl); if (resultError != null && updated.text.isBlank()) updated = updated.copy(text = "Tool-Fehler: $resultError"); if (updated.text.isBlank() && updated.imageUrl == null) updated = updated.copy(text = "Keine Antwort empfangen."); updated } }
                } else {
                    val messages = buildChatMessages(text)
                    val request = ChatRequest(model = _selectedModel.value, messages = messages, stream = true)
                    var sawOutput = false
                    withContext(Dispatchers.IO) {
                        val response = apiService.chatCompletionStream(request)
                        if (!response.isSuccessful) throw HttpException(response)
                        val body = response.body() ?: throw IllegalStateException("Response body is null")
                        body.byteStream().bufferedReader().useLines { lines ->
                            lines.forEach lineLoop@{ line ->
                                if (!line.startsWith("data:")) return@lineLoop
                                val payload = line.removePrefix("data:").trim()
                                if (payload.isBlank() || payload == "[DONE]") return@lineLoop
                                try {
                                    @Suppress("DEPRECATION") val root = JsonParser().parse(payload)
                                    if (!root.isJsonObject) return@lineLoop
                                    val chunk = root.asJsonObject.getAsJsonArray("choices")?.get(0)?.asJsonObject?.getAsJsonObject("delta")?.get("content")?.asString ?: ""
                                    if (chunk.isNotBlank()) {
                                        sawOutput = true
                                        withContext(Dispatchers.Main) { _messages.update { msgs -> msgs.map { if (it.id == assistantMessage.id) it.copy(text = it.text + chunk) else it } } }
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }
                    if (!sawOutput) { _messages.update { msgs -> msgs.map { if (it.id == assistantMessage.id && it.text.isBlank()) it.copy(text = "Keine Antwort empfangen.") else it } } }
                }
                val finalMessage = _messages.value.firstOrNull { it.id == assistantMessage.id }
                if (_speakResponses.value && finalMessage?.text?.isNotBlank() == true) voiceAgent?.speak(finalMessage.text)
            } catch (t: Throwable) {
                Log.e(TAG, "sendMessage failed", t)
                val errorText = when (t) {
                    is HttpException -> {
                        if (t.code() == 401 || t.code() == 403) {
                            _authRequired.value = true
                            "API-Token ist für dieses Modell und diesen Server erforderlich oder ungültig (Fehler 401/403). Bitte in den Einstellungen hinterlegen."
                        } else {
                            "Server-Fehler ${t.code()}"
                        }
                    }
                    else -> t.message ?: "Unbekannter Fehler"
                }
                _messages.update { msgs -> msgs.map { if (it.id == assistantMessage.id) it.copy(text = "Fehler: $errorText") else it } }
            } finally { _isLoading.value = false }
        }
    }

    fun loadModels() {
        viewModelScope.launch {
            _isModelLoading.value = true
            try {
                checkAuthRequiredSuspend()
                val apiService = getApiService() ?: throw IllegalStateException("Server nicht erreichbar")
                val modelsResponse = withContext(Dispatchers.IO) { apiService.getModels() }
                _models.value = modelsResponse.data
                if (_selectedModel.value.isBlank() && modelsResponse.data.isNotEmpty()) onModelSelected(modelsResponse.data.first().id)
            } catch (t: Throwable) {
                Log.e(TAG, "loadModels failed", t)
                val msg = if (t is HttpException && (t.code() == 401 || t.code() == 403)) "Token ungültig." else "Fehler beim Laden."
                postEvent(msg)
            } finally { _isModelLoading.value = false }
        }
    }

    fun checkAuthRequired() { viewModelScope.launch { checkAuthRequiredSuspend() } }

    private suspend fun checkAuthRequiredSuspend(): Boolean? {
        return try {
            val baseUrl = makeBaseUrl() ?: return null
            Log.d(TAG, "checkAuth: Probing WITHOUT token...")
            val serviceWithoutToken = RetrofitClient.getClient(baseUrl, null).create(LmStudioApiService::class.java)
            val response = withContext(Dispatchers.IO) { try { serviceWithoutToken.checkAuth() } catch (_: Exception) { null } }
            val result = when {
                response == null -> null
                response.code() == 401 || response.code() == 403 -> true
                response.isSuccessful -> false
                else -> null
            }
            _authRequired.value = result
            Log.d(TAG, "checkAuth probe result: $result")
            result
        } catch (t: Throwable) { Log.e(TAG, "checkAuthRequired failed", t); _authRequired.value = null; null }
    }

    fun clearChat() { _messages.value = emptyList() }
    fun saveChat() {
        viewModelScope.launch(Dispatchers.IO) {
            val content = _messages.value.joinToString("\n\n") { "${if (it.isUser) "Du" else "KI"}: ${it.text}${it.imageUrl?.let { " [Bild: $it]" } ?: ""}" }
            if (content.isBlank()) { postEvent("Der Chat ist leer."); return@launch }
            try { getApplication<Application>().openFileOutput("chat_history.txt", Context.MODE_PRIVATE).use { it.write(content.toByteArray()) }; postEvent("Chatverlauf gespeichert.") } catch (e: Exception) { postEvent("Fehler beim Speichern: ${e.message}") }
        }
    }

    fun setLmStudioIp(ip: String) { _lmStudioIp.value = ip.trim(); viewModelScope.launch { prefs.saveIp(ip.trim()); _authRequired.value = null } }
    fun setLmStudioPort(port: String) { _lmStudioPort.value = port.trim(); viewModelScope.launch { prefs.savePort(port.trim()); _authRequired.value = null } }
    fun setApiToken(token: String) { val trimmed = token.trim(); _apiToken.value = trimmed; _authRequired.value = null; viewModelScope.launch { prefs.saveApiToken(trimmed) } }
    fun setUseApiToken(enabled: Boolean) { _useApiToken.value = enabled; viewModelScope.launch { prefs.saveUseApiToken(enabled) } }
    fun setMcpServers(servers: String) { _mcpServers.value = servers; viewModelScope.launch { prefs.saveMcpServers(servers) } }
    fun setSystemPrompt(prompt: String) { _systemPrompt.value = prompt; viewModelScope.launch { prefs.saveSystemPrompt(prompt) } }
    fun onModelSelected(modelId: String) { _selectedModel.value = modelId; viewModelScope.launch { prefs.saveModel(modelId) } }
    fun setImageGenEnabled(enabled: Boolean) { _imageGenEnabled.value = enabled; viewModelScope.launch { prefs.saveImageGenEnabled(enabled) } }
    fun setFilesystemEnabled(enabled: Boolean) { _filesystemEnabled.value = enabled; viewModelScope.launch { prefs.saveFilesystemEnabled(enabled) } }
    fun setWebSearchEnabled(enabled: Boolean) { _webSearchEnabled.value = enabled; viewModelScope.launch { prefs.saveWebSearchEnabled(enabled) } }
    fun setCliMcpServerEnabled(enabled: Boolean) { _cliMcpServerEnabled.value = enabled; viewModelScope.launch { prefs.saveCliMcpServerEnabled(enabled) } }
    fun setBlenderEnabled(enabled: Boolean) { _blenderEnabled.value = enabled; viewModelScope.launch { prefs.saveBlenderEnabled(enabled) } }
    fun setDatetimeEnabled(enabled: Boolean) { _datetimeEnabled.value = enabled; viewModelScope.launch { prefs.saveDatetimeEnabled(enabled) } }
    fun toggleSpeakResponses() { _speakResponses.value = !_speakResponses.value; viewModelScope.launch { prefs.saveSpeakResponses(_speakResponses.value) } }
    fun startVoiceInput() { voiceAgent?.startListening() }
}
