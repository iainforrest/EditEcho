package com.example.editecho.network

import com.aallam.openai.api.audio.AudioSource
import com.aallam.openai.api.audio.audioTranscription
import com.aallam.openai.api.audio.ModelId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WhisperRepository {
    suspend fun transcribe(file: File): String = withContext(Dispatchers.IO) {
        OpenAIProvider.client.audioTranscription(
            audio = AudioSource(file),
            model = ModelId("whisper-1")
        ).text
    }
} 