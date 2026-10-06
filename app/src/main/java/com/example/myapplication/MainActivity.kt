package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.ChatScreen
import com.example.myapplication.ui.SettingsScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

private object Routes {
    const val CHAT = "chat"
    const val SETTINGS = "settings"
}

class MainActivity : ComponentActivity() {
    private var pendingMicrophoneAction: (() -> Unit)? = null

    private val requestMicrophonePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingMicrophoneAction?.invoke()
        } else {
            Toast.makeText(
                this,
                "Mikrofon-Zugriff nicht erteilt. Sprachfunktionen bleiben deaktiviert.",
                Toast.LENGTH_LONG
            ).show()
        }
        pendingMicrophoneAction = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val chatViewModel: ChatViewModel = viewModel()

                AppNavHost(
                    navController = navController,
                    chatViewModel = chatViewModel,
                    requestMicrophone = { action -> requestMicrophoneIfNeeded(action) }
                )
            }
        }
    }

    private fun requestMicrophoneIfNeeded(action: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            action()
        } else {
            pendingMicrophoneAction = action
            requestMicrophonePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    chatViewModel: ChatViewModel,
    requestMicrophone: (() -> Unit) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.CHAT,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Routes.CHAT) {
            ChatScreen(
                viewModel = chatViewModel,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onRequestMicrophone = requestMicrophone
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() },
                onRequestMicrophone = requestMicrophone
            )
        }
    }
}
