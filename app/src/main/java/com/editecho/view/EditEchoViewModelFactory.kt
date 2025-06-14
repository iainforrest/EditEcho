package com.editecho.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.editecho.data.SettingsRepository
import com.editecho.data.VoiceDNARepository
import com.editecho.network.AssistantApiClient
import com.editecho.network.ChatCompletionClient
import com.editecho.network.ClaudeCompletionClient
import com.editecho.network.DeepgramRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class EditEchoViewModelFactory @AssistedInject constructor(
    @Assisted private val context: Context,
    private val deepgramRepository: DeepgramRepository,
    private val assistantApiClient: AssistantApiClient,
    private val chatCompletionClient: ChatCompletionClient,
    private val claudeCompletionClient: ClaudeCompletionClient,
    private val settings: SettingsRepository,
    private val voiceDNARepository: VoiceDNARepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditEchoOverlayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditEchoOverlayViewModel(
                context,
                deepgramRepository,
                assistantApiClient,
                chatCompletionClient,
                claudeCompletionClient,
                settings,
                voiceDNARepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    @AssistedFactory
    interface Factory {
        fun create(context: Context): EditEchoViewModelFactory
    }
}