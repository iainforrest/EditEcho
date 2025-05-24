package com.editecho.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.editecho.data.SessionCounterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionCounterRepository: SessionCounterRepository
) : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId

    private var sessionStartTime: Long = 0L

    fun startRecording() {
        if (_isRecording.value) return

        viewModelScope.launch {
            try {
                val newSessionId = UUID.randomUUID().toString()
                _sessionId.value = newSessionId
                _isRecording.value = true
                sessionStartTime = System.currentTimeMillis()

                // Start session in Firestore
                sessionCounterRepository.startSession(newSessionId)
                    .addOnSuccessListener {
                        // Session started successfully
                    }
                    .addOnFailureListener { _ ->
                        // Handle error, maybe show a toast or log
                    }
            } catch (e: Exception) {
                // Handle any exceptions
            }
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return

        viewModelScope.launch {
            try {
                val sessionId = _sessionId.value ?: return@launch
                val duration = System.currentTimeMillis() - sessionStartTime
                
                // Update session duration in Firestore
                sessionCounterRepository.updateSessionDuration(sessionId, duration)
                    .addOnSuccessListener {
                        // Duration updated successfully
                    }
                    .addOnFailureListener { _ ->
                        // Handle error, maybe show a toast or log
                    }

                _isRecording.value = false
                _sessionId.value = null
            } catch (e: Exception) {
                // Handle any exceptions
            }
        }
    }
} 