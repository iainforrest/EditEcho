package com.editecho.ui.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.editecho.service.OverlayManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverlayViewModel @Inject constructor(
    private val overlayManager: OverlayManager
) : ViewModel() {

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    init {
        checkOverlayPermission()
    }

    private fun checkOverlayPermission() {
        _hasOverlayPermission.value = overlayManager.isOverlayPermissionGranted()
    }

    fun toggleOverlay() {
        viewModelScope.launch {
            if (_isOverlayActive.value) {
                overlayManager.stopOverlay()
                _isOverlayActive.value = false
            } else {
                if (!_hasOverlayPermission.value) {
                    overlayManager.requestOverlayPermission()
                    return@launch
                }
                overlayManager.startOverlay()
                _isOverlayActive.value = true
            }
        }
    }

    fun onOverlayPermissionResult() {
        checkOverlayPermission()
    }
} 