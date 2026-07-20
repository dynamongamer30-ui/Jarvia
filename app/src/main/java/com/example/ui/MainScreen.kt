package com.example.ui

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: JarvisViewModel,
    onNavigateSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val history by viewModel.history.collectAsState()
    val listState = rememberLazyListState()

    val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val permissionsState = rememberMultiplePermissionsState(permissions)
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            val serviceIntent = android.content.Intent(context, com.example.wakeword.WakeWordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size - 1)
        }
    }

    Scaffold(
        containerColor = Color(0xFF0A0A0F),
        topBar = {
            TopAppBar(
                title = { Text("Jarvis", color = Color.White) },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!permissionsState.allPermissionsGranted) {
                    Text(
                        "I need microphone and notification access to function.",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                        Text("Grant Permissions")
                    }
                } else {
                    OrbComponent(
                        state = uiState,
                        onTap = {
                            if (uiState == JarvisState.IDLE) viewModel.startListeningManual()
                            else if (uiState == JarvisState.LISTENING) viewModel.stopListeningManual()
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = when (uiState) {
                            JarvisState.IDLE -> "Tap orb or say 'Jarvis'"
                            JarvisState.LISTENING -> "Listening..."
                            JarvisState.THINKING -> "Thinking..."
                            JarvisState.SPEAKING -> "Speaking..."
                            JarvisState.ERROR -> "An error occurred"
                            JarvisState.SUCCESS -> "Action completed"
                        },
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Transcript Panel
            AnimatedVisibility(
                visible = transcript.isNotBlank() || history.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f)
                        ) {
                            items(history) { item ->
                                Text(
                                    text = item,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                        if (transcript.isNotBlank()) {
                            Text(
                                text = transcript,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
