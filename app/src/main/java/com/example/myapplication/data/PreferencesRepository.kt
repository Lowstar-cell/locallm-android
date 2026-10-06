package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.model.StoredPrompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private const val DATASTORE_NAME = "lmstudio_prefs"
internal val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

class PreferencesRepository(private val context: Context) {
    // Existing Keys
    private val keyIp = stringPreferencesKey("lmstudio_ip")
    private val keyPort = stringPreferencesKey("lmstudio_port")
    private val keyModel = stringPreferencesKey("selected_model")
    private val keySystemPrompt = stringPreferencesKey("system_prompt")
    private val keyPrompts = stringPreferencesKey("stored_prompts")
    private val keyApiToken = stringPreferencesKey("api_token")
    private val keyUseApiToken = booleanPreferencesKey("use_api_token")
    private val keyMcpServers = stringPreferencesKey("mcp_servers")
    private val keySpeakResponses = booleanPreferencesKey("speak_responses")

    // Keys for Server Toggles
    val imageGenEnabledKey = booleanPreferencesKey("image_gen_enabled")
    val filesystemEnabledKey = booleanPreferencesKey("filesystem_enabled")
    val webSearchEnabledKey = booleanPreferencesKey("web_search_enabled")
    val cliMcpServerEnabledKey = booleanPreferencesKey("cli_mcp_server_enabled")
    val blenderEnabledKey = booleanPreferencesKey("blender_enabled")
    val datetimeEnabledKey = booleanPreferencesKey("datetime_enabled")

    // Flows
    val ipFlow: Flow<String> = context.dataStore.data.map { it[keyIp] ?: "" }
    val portFlow: Flow<String> = context.dataStore.data.map { it[keyPort] ?: "" }
    val modelFlow: Flow<String> = context.dataStore.data.map { it[keyModel] ?: "" }
    val systemPromptFlow: Flow<String> = context.dataStore.data.map { it[keySystemPrompt] ?: "" }
    val apiTokenFlow: Flow<String> = context.dataStore.data.map { it[keyApiToken] ?: "" }
    val useApiTokenFlow: Flow<Boolean> = context.dataStore.data.map { it[keyUseApiToken] ?: false }
    val mcpServersFlow: Flow<String> = context.dataStore.data.map { it[keyMcpServers] ?: "" }
    val speakResponsesFlow: Flow<Boolean> = context.dataStore.data.map { it[keySpeakResponses] ?: false }
    
    val savedPromptsFlow: Flow<List<StoredPrompt>> = context.dataStore.data.map { prefs ->
        prefs[keyPrompts]?.let { decodePrompts(it) } ?: emptyList()
    }.flowOn(Dispatchers.IO) // Run decoding on a background thread

    val imageGenEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[imageGenEnabledKey] ?: false }
    val filesystemEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[filesystemEnabledKey] ?: false }
    val webSearchEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[webSearchEnabledKey] ?: false }
    val cliMcpServerEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[cliMcpServerEnabledKey] ?: false }
    val blenderEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[blenderEnabledKey] ?: false }
    val datetimeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[datetimeEnabledKey] ?: false }

    // Save Functions
    suspend fun saveIp(value: String) {
        context.dataStore.edit { it[keyIp] = value }
    }

    suspend fun savePort(value: String) {
        context.dataStore.edit { it[keyPort] = value }
    }

    suspend fun saveModel(value: String) {
        context.dataStore.edit { it[keyModel] = value }
    }

    suspend fun saveSystemPrompt(value: String) {
        context.dataStore.edit { it[keySystemPrompt] = value }
    }

    suspend fun saveApiToken(value: String) {
        context.dataStore.edit { it[keyApiToken] = value }
    }

    suspend fun saveUseApiToken(value: Boolean) {
        context.dataStore.edit { it[keyUseApiToken] = value }
    }

    suspend fun saveMcpServers(value: String) {
        context.dataStore.edit { it[keyMcpServers] = value }
    }

    suspend fun saveSpeakResponses(value: Boolean) {
        context.dataStore.edit { it[keySpeakResponses] = value }
    }

    suspend fun savePrompts(list: List<StoredPrompt>) {
        context.dataStore.edit { prefs ->
            prefs[keyPrompts] = encodePrompts(list)
        }
    }

    suspend fun saveImageGenEnabled(value: Boolean) { context.dataStore.edit { it[imageGenEnabledKey] = value } }
    suspend fun saveFilesystemEnabled(value: Boolean) { context.dataStore.edit { it[filesystemEnabledKey] = value } }
    suspend fun saveWebSearchEnabled(value: Boolean) { context.dataStore.edit { it[webSearchEnabledKey] = value } }
    suspend fun saveCliMcpServerEnabled(value: Boolean) { context.dataStore.edit { it[cliMcpServerEnabledKey] = value } }
    suspend fun saveBlenderEnabled(value: Boolean) { context.dataStore.edit { it[blenderEnabledKey] = value } }
    suspend fun saveDatetimeEnabled(value: Boolean) { context.dataStore.edit { it[datetimeEnabledKey] = value } }

    // Helper Functions
    private fun decodePrompts(raw: String): List<StoredPrompt> {
        // This is a synchronous function and can block if 'raw' is large.
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map {
                val obj = array.getJSONObject(it)
                StoredPrompt(obj.getString("title"), obj.getString("text"))
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun encodePrompts(list: List<StoredPrompt>): String {
        val array = JSONArray()
        list.forEach { prompt ->
            array.put(JSONObject().apply {
                put("title", prompt.title)
                put("text", prompt.text)
            })
        }
        return array.toString()
    }
}
