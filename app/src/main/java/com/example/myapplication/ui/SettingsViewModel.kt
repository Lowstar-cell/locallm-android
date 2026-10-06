package com.example.myapplication.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesRepository(application)

    val imageGenEnabled: StateFlow<Boolean> = prefs.imageGenEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val filesystemEnabled: StateFlow<Boolean> = prefs.filesystemEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val webSearchEnabled: StateFlow<Boolean> = prefs.webSearchEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val cliMcpServerEnabled: StateFlow<Boolean> = prefs.cliMcpServerEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val blenderEnabled: StateFlow<Boolean> = prefs.blenderEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val datetimeEnabled: StateFlow<Boolean> = prefs.datetimeEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setImageGenEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.saveImageGenEnabled(enabled) }
    }

    fun setFilesystemEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.saveFilesystemEnabled(enabled) }
    }

    fun setWebSearchEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.saveWebSearchEnabled(enabled) }
    }

    fun setCliMcpServerEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.saveCliMcpServerEnabled(enabled) }
    }

    fun setBlenderEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.saveBlenderEnabled(enabled) }
    }

    fun setDatetimeEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.saveDatetimeEnabled(enabled) }
    }
}