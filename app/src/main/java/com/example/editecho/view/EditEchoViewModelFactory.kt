package com.example.editecho.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.editecho.network.AssistantApiClient
import com.example.editecho.network.WhisperRepository

class EditEchoViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditEchoOverlayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditEchoOverlayViewModel(
                context,
                WhisperRepository(),
                AssistantApiClient()          // ← no more OpenAIProvider.client
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}