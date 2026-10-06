package com.example.myapplication.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.selection.SelectionContainer

import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import com.example.myapplication.ChatViewModel
import com.example.myapplication.R
import com.example.myapplication.model.Message
import com.example.myapplication.ui.theme.Border
import com.example.myapplication.ui.theme.Cyan
import com.example.myapplication.ui.theme.Danger
import com.example.myapplication.ui.theme.ElevatedSurface
import com.example.myapplication.ui.theme.Ink
import com.example.myapplication.ui.theme.RaisedSurface
import com.example.myapplication.ui.theme.SignalBlue
import com.example.myapplication.ui.theme.Success
import com.example.myapplication.ui.theme.TextBright
import com.example.myapplication.ui.theme.TextFaint
import com.example.myapplication.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenSettings: () -> Unit,
    onRequestMicrophone: (() -> Unit) -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val host by viewModel.lmStudioIp.collectAsState()
    val authRequired by viewModel.authRequired.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var input by rememberSaveable { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    fun submit() {
        val value = input.trim()
        if (value.isNotEmpty() && !isLoading) {
            viewModel.sendMessage(value)
            input = ""
            keyboardController?.hide()
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ChatTopBar(
                selectedModel = selectedModel,
                host = host,
                authRequired = authRequired,
                menuExpanded = menuExpanded,
                onMenuExpandedChange = { menuExpanded = it },
                onOpenSettings = onOpenSettings,
                onSaveChat = viewModel::saveChat,
                onClearChat = { showClearDialog = true }
            )
        },
        bottomBar = {
            ChatComposer(
                input = input,
                onInputChange = { input = it },
                onSend = ::submit,
                isLoading = isLoading,
                onVoice = { onRequestMicrophone { viewModel.startVoiceInput() } },
                focusRequester = focusRequester
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (messages.isEmpty()) {
                EmptyChatState(
                    onSuggestion = { suggestion ->
                        input = suggestion
                        focusRequester.requestFocus()
                    },
                    onOpenSettings = onOpenSettings
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageRow(
                            message = message,
                            isStreaming = isLoading && message.id == messages.lastOrNull()?.id && !message.isUser
                        )
                    }
                }
            }
        }
    }
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
            title = { Text("Chat wirklich leeren?") },
            text = { Text("Alle Nachrichten werden aus dieser Ansicht entfernt. Ein gespeicherter Chatverlauf bleibt unberührt.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearChat(); showClearDialog = false }) {
                    Text("Leeren", color = Danger)
                }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Abbrechen") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    selectedModel: String,
    host: String,
    authRequired: Boolean?,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onSaveChat: () -> Unit,
    onClearChat: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.logo1152ohnebg),
                    contentDescription = "LocalLM",
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(ElevatedSurface).padding(5.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("LocalLM", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(7.dp).clip(CircleShape).background(
                                when (authRequired) { true -> Danger; false -> Success; null -> TextFaint }
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = when {
                                selectedModel.isNotBlank() -> selectedModel
                                host.isNotBlank() -> "Modell auswählen"
                                else -> "Bereit für deine nächste Idee"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        actions = {
            Box {
                IconButton(onClick = { onMenuExpandedChange(true) }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Weitere Optionen")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { onMenuExpandedChange(false) }) {
                    DropdownMenuItem(
                        text = { Text("Chatverlauf speichern") },
                        leadingIcon = { Icon(Icons.Outlined.Save, contentDescription = null) },
                        onClick = { onMenuExpandedChange(false); onSaveChat() }
                    )
                    DropdownMenuItem(
                        text = { Text("Chat leeren") },
                        leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
                        onClick = { onMenuExpandedChange(false); onClearChat() }
                    )
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "Einstellungen öffnen")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatComposer(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    onVoice: () -> Unit,
    focusRequester: FocusRequester
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onVoice, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.Mic, contentDescription = "Spracheingabe starten", tint = SignalBlue)
            }
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                placeholder = { Text("Nachricht an LocalLM …") },
                shape = RoundedCornerShape(18.dp),
                minLines = 1,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
            FilledIconButton(onClick = onSend, enabled = input.isNotBlank() && !isLoading, modifier = Modifier.size(52.dp)) {
                Icon(imageVector = if (isLoading) Icons.Outlined.Close else Icons.AutoMirrored.Outlined.Send, contentDescription = "Nachricht senden")
            }
        }
    }
}


@Composable
private fun EmptyChatState(
    onSuggestion: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
            color = SignalBlue.copy(alpha = 0.14f),
            border = androidx.compose.foundation.BorderStroke(1.dp, SignalBlue.copy(alpha = 0.35f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = SignalBlue, modifier = Modifier.size(34.dp))
            }
        }
        Spacer(Modifier.size(22.dp))
        Text("Dein lokaler Workspace", style = MaterialTheme.typography.headlineMedium, color = TextBright)
        Spacer(Modifier.size(10.dp))
        Text(
            "Private, schnelle und direkt auf deinem Rechner. Was möchtest du heute bauen?",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.size(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { onSuggestion("Hilf mir, eine Idee zu strukturieren") },
                label = { Text("Idee strukturieren") },
                leadingIcon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            AssistChip(
                onClick = { onSuggestion("Schreibe mir einen kurzen Projektplan") },
                label = { Text("Projektplan erstellen") },
                leadingIcon = { Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            AssistChip(
                onClick = { onSuggestion("Erkläre mir ein komplexes Thema") },
                label = { Text("Thema verständlich erklären") },
                leadingIcon = { Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }
        Spacer(Modifier.size(18.dp))
        TextButton(onClick = onOpenSettings) {
            Text("Server und Modell einrichten", color = SignalBlue)
        }
    }
}

@Composable
private fun MessageRow(message: Message, isStreaming: Boolean) {
    val isUser = message.isUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = SignalBlue.copy(alpha = 0.16f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = SignalBlue, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = if (isUser) "DU" else "LOCALLM",
                style = MaterialTheme.typography.labelSmall,
                color = if (isUser) TextMuted else SignalBlue,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.size(6.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .widthIn(max = 620.dp)
                .animateContentSize(),
            shape = if (isUser) RoundedCornerShape(20.dp, 20.dp, 5.dp, 20.dp) else RoundedCornerShape(20.dp, 20.dp, 20.dp, 5.dp),
            color = if (isUser) SignalBlue else ElevatedSurface,
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, Border.copy(alpha = 0.7f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
                if (message.text.isNotBlank()) {
                    SelectionContainer {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isUser) Ink else TextBright
                        )
                    }
                } else if (isStreaming) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = if (isUser) Ink else SignalBlue)
                } else {
                    Text("Keine Antwort empfangen.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                }
                message.imageUrl?.let { imageUrl ->
                    Spacer(Modifier.size(12.dp))
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Erzeugtes Bild",
                        modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 360.dp).clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

