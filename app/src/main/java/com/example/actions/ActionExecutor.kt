package com.example.actions

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.widget.Toast
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class ActionExecutor(private val context: Context) {
    
    fun execute(intent: JsonObject): ActionResponse {
        val name = intent["name"]?.jsonPrimitive?.content ?: return ActionResponse.Error("Unknown intent")
        val args = intent["args"] as? JsonObject
        
        return when (name) {
            "open_app" -> {
                val appName = args?.get("app_name")?.jsonPrimitive?.content
                // Simple logic for prototype: just show toast
                // Real app would use PackageManager to find package by name
                Toast.makeText(context, "Opening app: $appName", Toast.LENGTH_SHORT).show()
                ActionResponse.Success("Opening $appName")
            }
            "toggle_setting" -> {
                val setting = args?.get("setting")?.jsonPrimitive?.content
                val state = args?.get("state")?.jsonPrimitive?.content
                // Use Shizuku or system APIs
                Toast.makeText(context, "Toggled $setting $state", Toast.LENGTH_SHORT).show()
                ActionResponse.Success("Turned $setting $state")
            }
            "send_message" -> {
                val contact = args?.get("contact")?.jsonPrimitive?.content
                val msg = args?.get("message")?.jsonPrimitive?.content
                val channel = args?.get("channel")?.jsonPrimitive?.content
                Toast.makeText(context, "Sent $msg to $contact via $channel", Toast.LENGTH_LONG).show()
                ActionResponse.Success("Message sent to $contact")
            }
            "set_volume" -> {
                val level = args?.get("level")?.jsonPrimitive?.content?.toIntOrNull()
                level?.let {
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val target = (it / 100f * max).toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
                }
                ActionResponse.Success("Volume set to $level percent")
            }
            "get_info", "reply_only" -> {
                val reply = args?.get("reply")?.jsonPrimitive?.content
                ActionResponse.Success(reply ?: "I found the information.")
            }
            else -> ActionResponse.Error("Unsupported action: $name")
        }
    }
}

sealed class ActionResponse {
    data class Success(val replyText: String) : ActionResponse()
    data class Error(val reason: String) : ActionResponse()
    data class RequiresConfirmation(val prompt: String, val pendingAction: JsonObject) : ActionResponse()
}
