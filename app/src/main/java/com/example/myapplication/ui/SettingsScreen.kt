package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.ChatViewModel
import com.example.myapplication.model.ModelInfo
import com.example.myapplication.ui.theme.Danger
import com.example.myapplication.ui.theme.SignalBlue
import com.example.myapplication.ui.theme.Success
import com.example.myapplication.ui.theme.TextBright
import com.example.myapplication.ui.theme.TextFaint
import com.example.myapplication.ui.theme.TextMuted

import com.example.myapplication.ui.theme.ElevatedSurface
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    onRequestMicrophone: (() -> Unit) -> Unit
) {
    val host by viewModel.lmStudioIp.collectAsState()
    val port by viewModel.lmStudioPort.collectAsState()
    val token by viewModel.apiToken.collectAsState()
    val useApiToken by viewModel.useApiToken.collectAsState()
    val models by viewModel.models.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val loadingModels by viewModel.isModelLoading.collectAsState()
    val authRequired by viewModel.authRequired.collectAsState()
    val prompt by viewModel.systemPrompt.collectAsState()
    val mcp by viewModel.mcpServers.collectAsState()
    val imageGen by viewModel.imageGenEnabled.collectAsState()
    val filesystem by viewModel.filesystemEnabled.collectAsState()
    val webSearch by viewModel.webSearchEnabled.collectAsState()
    val cli by viewModel.cliMcpServerEnabled.collectAsState()
    val blender by viewModel.blenderEnabled.collectAsState()
    val datetime by viewModel.datetimeEnabled.collectAsState()
    val speak by viewModel.speakResponses.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var tokenVisible by rememberSaveable { mutableStateOf(false) }
    var modelMenu by remember { mutableStateOf(false) }
    var clearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.events.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(host, port) {
        if (host.isNotBlank() && authRequired == null) {
            viewModel.checkAuthRequired()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Einstellungen", style = MaterialTheme.typography.titleLarge, color = TextBright)
                        Text("LocalLM konfigurieren", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Zurück") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConnectionCard(host, port, token, tokenVisible, useApiToken, viewModel::setLmStudioIp, viewModel::setLmStudioPort, viewModel::setApiToken, viewModel::setUseApiToken, { tokenVisible = !tokenVisible }, viewModel::checkAuthRequired) }
            item { ModelCard(models, selectedModel, loadingModels, modelMenu, { modelMenu = it }, viewModel::loadModels, viewModel::onModelSelected) }
            item { PromptCard(prompt, viewModel::setSystemPrompt) }
            item { McpCard(mcp, imageGen, filesystem, webSearch, cli, blender, datetime, viewModel::setMcpServers, viewModel::setImageGenEnabled, viewModel::setFilesystemEnabled, viewModel::setWebSearchEnabled, viewModel::setCliMcpServerEnabled, viewModel::setBlenderEnabled, viewModel::setDatetimeEnabled) }
            item { VoiceCard(speak, viewModel::toggleSpeakResponses, onRequestMicrophone) }
            item { DataCard(messages.size, viewModel::saveChat) { clearDialog = true } }
            item { Text("LocalLM · Deine Daten bleiben auf diesem Gerät und werden nur an den von dir konfigurierten Server gesendet.", style = MaterialTheme.typography.bodySmall, color = TextFaint, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) }
        }
    }

    if (clearDialog) {
        AlertDialog(
            onDismissRequest = { clearDialog = false },
            icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
            title = { Text("Chat leeren?") },
            text = { Text("Die aktuelle Chatansicht wird gelöscht. Bereits gespeicherte Dateien bleiben erhalten.") },
            confirmButton = { TextButton(onClick = { viewModel.clearChat(); clearDialog = false }) { Text("Leeren", color = Danger) } },
            dismissButton = { TextButton(onClick = { clearDialog = false }) { Text("Abbrechen") } }
        )
    }
}

@Composable
private fun SettingsCard(icon: ImageVector, title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ElevatedSurface)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(38.dp), shape = RoundedCornerShape(12.dp), color = SignalBlue.copy(alpha = 0.14f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = SignalBlue) }
                }
                Spacer(Modifier.width(12.dp))
                Column { Text(title, style = MaterialTheme.typography.titleMedium, color = TextBright); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted) }
            }
            Spacer(Modifier.size(16.dp))
            content()
        }
    }
}

@Composable
private fun ToggleRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (checked) SignalBlue else TextFaint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.bodyLarge, color = TextBright); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted) }
        Switch(checked, onCheckedChange)
    }
}

@Composable
private fun ConnectionCard(
    host: String,
    port: String,
    token: String,
    tokenVisible: Boolean,
    useApiToken: Boolean,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onTokenChange: (String) -> Unit,
    onUseApiTokenChange: (Boolean) -> Unit,
    onToggleToken: () -> Unit,
    onTest: () -> Unit
) {
    SettingsCard(Icons.Outlined.Cloud, "Verbindung", "LM Studio im lokalen Netzwerk") {
        OutlinedTextField(host, { onHostChange(it) }, Modifier.fillMaxWidth(), label = { Text("Host oder IP") }, singleLine = true, leadingIcon = { Icon(Icons.Outlined.Cloud, null) })
        Spacer(Modifier.size(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(port, { onPortChange(it) }, Modifier.weight(1f), label = { Text("Port") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Button(onClick = onTest, modifier = Modifier.height(56.dp), enabled = host.isNotBlank()) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Testen") }
        }
        Spacer(Modifier.size(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Key, null, tint = if (useApiToken) SignalBlue else TextFaint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("API-Token verwenden", style = MaterialTheme.typography.bodyLarge, color = TextBright)
                Text("Aktivieren, wenn der Server ein Token erfordert", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Switch(checked = useApiToken, onCheckedChange = onUseApiTokenChange)
        }
        if (useApiToken) {
            Spacer(Modifier.size(10.dp))
            OutlinedTextField(
                value = token,
                onValueChange = onTokenChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API-Token") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Key, null) },
                trailingIcon = { IconButton(onClick = onToggleToken) { Icon(if (tokenVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (tokenVisible) "Token verbergen" else "Token anzeigen") } },
                visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation()
            )
        }
    }
}

@Composable
private fun ModelCard(models: List<ModelInfo>, selectedModel: String, loading: Boolean, menuExpanded: Boolean, onMenuExpandedChange: (Boolean) -> Unit, onLoad: () -> Unit, onSelect: (String) -> Unit) {
    SettingsCard(Icons.Outlined.AutoAwesome, "Modell", "Wähle ein verfügbares LocalLM-Modell") {
        OutlinedButton(onClick = onLoad, enabled = !loading, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(8.dp)); Text(if (loading) "Modelle werden geladen …" else "Modelle laden") }
        Spacer(Modifier.size(10.dp))
        Box {
            OutlinedButton(onClick = { onMenuExpandedChange(true) }, modifier = Modifier.fillMaxWidth(), enabled = models.isNotEmpty()) { Text(selectedModel.ifBlank { "Kein Modell ausgewählt" }, Modifier.weight(1f), maxLines = 1); Icon(Icons.Outlined.ExpandMore, null) }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { onMenuExpandedChange(false) }) {
                models.forEach { model -> DropdownMenuItem(text = { Text(model.id, maxLines = 1) }, onClick = { onSelect(model.id); onMenuExpandedChange(false) }, leadingIcon = { if (model.id == selectedModel) Icon(Icons.Outlined.Check, null) }) }
            }
        }
    }
}


@Composable
private fun PromptCard(prompt: String, onPromptChange: (String) -> Unit) {
    SettingsCard(Icons.Outlined.Info, "System-Prompt", "Optionaler Kontext für jede Antwort") {
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            label = { Text("Instruktionen für das Modell") },
            placeholder = { Text("z. B. Antworte immer auf Deutsch …") }
        )
    }
}

@Composable
private fun McpCard(
    manualServers: String,
    imageGenEnabled: Boolean,
    filesystemEnabled: Boolean,
    webSearchEnabled: Boolean,
    cliEnabled: Boolean,
    blenderEnabled: Boolean,
    datetimeEnabled: Boolean,
    onManualServersChange: (String) -> Unit,
    onImageGenChange: (Boolean) -> Unit,
    onFilesystemChange: (Boolean) -> Unit,
    onWebSearchChange: (Boolean) -> Unit,
    onCliChange: (Boolean) -> Unit,
    onBlenderChange: (Boolean) -> Unit,
    onDatetimeChange: (Boolean) -> Unit
) {
    SettingsCard(Icons.Outlined.Tune, "MCP-Funktionen", "Optionale Werkzeuche für das Modell") {
        OutlinedTextField(
            value = manualServers,
            onValueChange = onManualServersChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Eigene MCP-Server") },
            placeholder = { Text("playwright, custom-server") },
            supportingText = { Text("Mehrere Server mit Komma trennen") }
        )
        Spacer(Modifier.size(8.dp))
        ToggleRow(Icons.Outlined.Palette, "Bildgenerierung", "Bilder erzeugen und anzeigen", imageGenEnabled, onImageGenChange)
        ToggleRow(Icons.Outlined.Folder, "Dateisystem", "Lokale Dateien verwalten", filesystemEnabled, onFilesystemChange)
        ToggleRow(Icons.Outlined.Search, "Web-Suche", "Aktuelle Informationen abrufen", webSearchEnabled, onWebSearchChange)
        ToggleRow(Icons.Outlined.Terminal, "CLI Server", "Befehle in kontrollierter Umgebung", cliEnabled, onCliChange)
        ToggleRow(Icons.Outlined.AutoAwesome, "Blender", "Blender-Szenen und Objekte", blenderEnabled, onBlenderChange)
        ToggleRow(Icons.Outlined.Schedule, "Datum & Zeit", "Aktuellen Kontext liefern", datetimeEnabled, onDatetimeChange)
    }
}


@Composable
private fun VoiceCard(speakResponses: Boolean, onToggleSpeakResponses: () -> Unit, onRequestMicrophone: (() -> Unit) -> Unit) {
    SettingsCard(Icons.Outlined.RecordVoiceOver, "Sprache", "Optionale Sprachausgabe und Spracheingabe") {
        ToggleRow(Icons.Outlined.RecordVoiceOver, "Antworten vorlesen", "Antworten über die Geräteausgabe sprechen", speakResponses, { onToggleSpeakResponses() })
        Spacer(Modifier.size(8.dp))
        OutlinedButton(onClick = { onRequestMicrophone { } }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Mic, null)
            Spacer(Modifier.width(8.dp))
            Text("Mikrofon-Berechtigung prüfen")
        }
    }
}

@Composable
private fun DataCard(messageCount: Int, onSave: () -> Unit, onClear: () -> Unit) {
    SettingsCard(Icons.Outlined.Save, "Chatdaten", "Lokale Verwaltung deiner Unterhaltung") {
        Text("$messageCount Nachrichten in dieser Sitzung", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        Spacer(Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.Save, null); Spacer(Modifier.width(6.dp)); Text("Speichern") }
            OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.DeleteOutline, null, tint = Danger); Spacer(Modifier.width(6.dp)); Text("Leeren", color = Danger) }
        }
    }
}

