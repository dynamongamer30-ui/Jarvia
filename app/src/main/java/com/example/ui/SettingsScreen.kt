package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.settings.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    
    var geminiKey by remember { mutableStateOf(settingsManager.geminiApiKey) }
    var ttsPitch by remember { mutableStateOf(settingsManager.ttsPitch) }
    var ttsSpeed by remember { mutableStateOf(settingsManager.ttsSpeed) }
    var onDeviceOnly by remember { mutableStateOf(settingsManager.onDeviceOnly) }

    Scaffold(
        containerColor = Color(0xFF0A0A0F),
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("API Keys", color = Color(0xFF00E5FF), style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = geminiKey,
                onValueChange = { 
                    geminiKey = it
                    settingsManager.geminiApiKey = it 
                },
                label = { Text("Gemini API Key") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedLabelColor = Color.LightGray
                )
            )

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("On-device only (Disable Cloud API)", color = Color.White, modifier = Modifier.weight(1f))
                Switch(
                    checked = onDeviceOnly,
                    onCheckedChange = { 
                        onDeviceOnly = it
                        settingsManager.onDeviceOnly = it
                    }
                )
            }
            
            HorizontalDivider(color = Color.DarkGray)
            
            Text("Voice Settings (TTS)", color = Color(0xFF00E5FF), style = MaterialTheme.typography.titleMedium)
            
            Text("Pitch: ${String.format("%.1f", ttsPitch)}", color = Color.White)
            Slider(
                value = ttsPitch,
                onValueChange = { 
                    ttsPitch = it
                    settingsManager.ttsPitch = it 
                },
                valueRange = 0.5f..2.0f
            )

            Text("Speed: ${String.format("%.1f", ttsSpeed)}", color = Color.White)
            Slider(
                value = ttsSpeed,
                onValueChange = { 
                    ttsSpeed = it
                    settingsManager.ttsSpeed = it 
                },
                valueRange = 0.5f..2.0f
            )
            
            HorizontalDivider(color = Color.DarkGray)
            
            Text("Permissions & Security", color = Color(0xFF00E5FF), style = MaterialTheme.typography.titleMedium)
            Text(
                "Accessibility Service Usage: Jarvis uses the Accessibility Service to perform clicks and navigate through third-party apps on your behalf when no direct API is available. It can read the screen content to find buttons and text fields to execute your voice commands. This data is processed on-device and never stored or transmitted. Please enable it in your system settings.", 
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Shizuku: Not connected", color = Color.LightGray)
        }
    }
}
