package com.editecho.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.editecho.data.SettingsRepository
import com.editecho.network.AssistantApiClient
import com.editecho.network.ChatCompletionClient
import com.editecho.network.WhisperRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class EditEchoViewModelFactory @AssistedInject constructor(
    @Assisted private val context: Context,
    private val whisperRepository: WhisperRepository,
    private val assistantApiClient: AssistantApiClient,
    private val chatCompletionClient: ChatCompletionClient,
    private val settings: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditEchoOverlayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditEchoOverlayViewModel(
                context,
                whisperRepository,
                assistantApiClient,
                chatCompletionClient,
                settings
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    @AssistedFactory
    interface Factory {
        fun create(context: Context): EditEchoViewModelFactory
    }
}