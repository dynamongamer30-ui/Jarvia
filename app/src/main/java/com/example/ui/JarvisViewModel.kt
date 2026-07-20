package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.actions.ActionExecutor
import com.example.actions.ActionResponse
import com.example.nlu.GeminiClient
import com.example.settings.SettingsManager
import com.example.stt.SpeechRecognizerManager
import com.example.tts.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class JarvisState {
    IDLE, LISTENING, THINKING, SPEAKING, ERROR, SUCCESS
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(JarvisState.IDLE)
    val uiState: StateFlow<JarvisState> = _uiState.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    private val settingsManager = SettingsManager(application)
    private val actionExecutor = ActionExecutor(application)
    private val geminiClient = GeminiClient()
    
    private val ttsManager = TextToSpeechManager(application, settingsManager) { isSpeaking ->
        if (isSpeaking) {
            _uiState.value = JarvisState.SPEAKING
        } else if (_uiState.value == JarvisState.SPEAKING) {
            _uiState.value = JarvisState.IDLE
        }
    }
    
    private val sttManager = SpeechRecognizerManager(
        context = application,
        onPartialResult = { text -> 
            _transcript.value = text 
        },
        onFinalResult = { text ->
            _transcript.value = text
            processCommand(text)
        },
        onError = { error ->
            _uiState.value = JarvisState.ERROR
            addToHistory("Error: $error")
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                _uiState.value = JarvisState.IDLE
            }
        },
        onStateChanged = { isListening ->
            if (isListening) _uiState.value = JarvisState.LISTENING
        }
    )

    fun onWakeWordDetected() {
        ttsManager.stop()
        _transcript.value = ""
        sttManager.startListening()
    }

    fun startListeningManual() {
        ttsManager.stop()
        _transcript.value = ""
        sttManager.startListening()
    }

    fun stopListeningManual() {
        sttManager.stopListening()
    }

    private fun processCommand(text: String) {
        if (text.isBlank()) {
            _uiState.value = JarvisState.IDLE
            return
        }
        addToHistory("User: $text")
        _uiState.value = JarvisState.THINKING
        
        viewModelScope.launch {
            val apiKey = settingsManager.geminiApiKey
            if (apiKey.isBlank()) {
                handleActionResponse(ActionResponse.Error("API key not configured"))
                return@launch
            }
            
            val intent = geminiClient.parseIntent(apiKey, text)
            if (intent != null) {
                val response = actionExecutor.execute(intent)
                handleActionResponse(response)
            } else {
                handleActionResponse(ActionResponse.Error("Failed to parse intent"))
            }
        }
    }

    private fun handleActionResponse(response: ActionResponse) {
        when (response) {
            is ActionResponse.Success -> {
                _uiState.value = JarvisState.SUCCESS
                addToHistory("Jarvis: ${response.replyText}")
                ttsManager.speak(response.replyText)
            }
            is ActionResponse.Error -> {
                _uiState.value = JarvisState.ERROR
                addToHistory("Jarvis Error: ${response.reason}")
                ttsManager.speak(response.reason)
            }
            is ActionResponse.RequiresConfirmation -> {
                _uiState.value = JarvisState.SPEAKING
                addToHistory("Jarvis: ${response.prompt}")
                ttsManager.speak(response.prompt)
                // In a real implementation, we would wait for confirmation here
            }
        }
    }

    private fun addToHistory(msg: String) {
        _history.value = _history.value + msg
    }

    override fun onCleared() {
        super.onCleared()
        sttManager.destroy()
        ttsManager.shutdown()
    }
}
