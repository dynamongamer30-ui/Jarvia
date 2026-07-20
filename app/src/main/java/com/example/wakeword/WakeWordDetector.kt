package com.example.wakeword

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
// import org.tensorflow.lite.Interpreter

class WakeWordDetector(private val context: Context, private val onDetected: () -> Unit) {
    
    private var isRecording = false
    private var audioRecord: AudioRecord? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    // For a real openWakeWord integration we would load the .tflite model from assets
    // private var tflite: Interpreter? = null

    fun start() {
        if (isRecording) return
        
        // Mock loading TFLite model
        /*
        val assetFileDescriptor = context.assets.openFd("jarvis_wakeword.tflite")
        val fileChannel = FileInputStream(assetFileDescriptor.fileDescriptor).channel
        val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, assetFileDescriptor.startOffset, assetFileDescriptor.declaredLength)
        tflite = Interpreter(mappedByteBuffer)
        */

        val sampleRate = 16000
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return
            }
            
            audioRecord?.startRecording()
            isRecording = true
            
            scope.launch {
                val buffer = ShortArray(bufferSize)
                try {
                    while (isActive && isRecording) {
                        val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (read > 0) {
                            // Mock detection logic: just check if we hit a random loud noise for testing
                            // In reality, run TFLite inference: tflite.run(input, output)
                        }
                    }
                } finally {
                    stop()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            stop()
        }
    }
    
    fun stop() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
