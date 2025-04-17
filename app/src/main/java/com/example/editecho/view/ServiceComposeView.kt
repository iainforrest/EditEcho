package com.example.editecho.view

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A custom view that provides lifecycle management for Compose views in a service context.
 * This class implements LifecycleOwner, ViewModelStoreOwner, and SavedStateRegistryOwner to provide
 * all necessary components for Compose to function properly outside of an Activity context.
 */
class ServiceComposeView(context: Context) : FrameLayout(context), 
    LifecycleOwner, 
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val composeView = ComposeView(context)
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val _viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    init {
        // Add the ComposeView to our FrameLayout
        addView(composeView)

        // Initialize lifecycle state
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        // Initialize saved state registry
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = _viewModelStore

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        _viewModelStore.clear()
    }

    fun setComposableContent(content: @Composable () -> Unit) {
        composeView.setContent {
            CompositionLocalProvider(
                LocalLifecycleOwner provides this,
                LocalViewModelStoreOwner provides this,
                LocalSavedStateRegistryOwner provides this
            ) {
                content()
            }
        }
    }
}

// Define CompositionLocals for our owners
val LocalLifecycleOwner = staticCompositionLocalOf<LifecycleOwner> {
    error("No LifecycleOwner provided")
}

val LocalViewModelStoreOwner = staticCompositionLocalOf<ViewModelStoreOwner> {
    error("No ViewModelStoreOwner provided")
}

val LocalSavedStateRegistryOwner = staticCompositionLocalOf<SavedStateRegistryOwner> {
    error("No SavedStateRegistryOwner provided")
} 