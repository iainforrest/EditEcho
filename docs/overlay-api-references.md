# EditEcho Overlay API Documentation References

## Overview
This document contains official Android API documentation links for implementing the keyboard-style overlay feature in EditEcho. These references cover WindowManager, permissions, foreground services, and Jetpack Compose integration.

## Core Window Management

### 1. WindowManager API
- **URL**: https://developer.android.com/reference/android/view/WindowManager
- **Purpose**: Main interface for interacting with the window manager
- **Key Methods**: `addView()`, `removeView()`, `updateViewLayout()`
- **Usage**: Creating and managing system-level overlay windows

### 2. ViewManager Interface
- **URL**: https://developer.android.com/reference/android/view/ViewManager
- **Purpose**: Interface to add and remove child views from the associated window
- **Key Methods**: `addView()`, `removeView()`, `updateViewLayout()`
- **Usage**: Base interface that WindowManager extends

### 3. WindowManager.LayoutParams
- **URL**: https://developer.android.com/reference/android/view/WindowManager.LayoutParams
- **Purpose**: Parameters for window positioning, behavior, and properties
- **Key Constants**:
  - `TYPE_APPLICATION_OVERLAY` - Window type for overlays (API 26+)
  - `FLAG_NOT_FOCUSABLE` - Window won't receive input focus
  - `FLAG_NOT_TOUCH_MODAL` - Touches outside window pass through
  - `FLAG_ALT_FOCUSABLE_IM` - Window positioned to remain visible when soft keyboard shown
- **Usage**: Configuring overlay behavior and positioning

## Permission System

### 4. SYSTEM_ALERT_WINDOW in Android 11
- **URL**: https://developer.android.com/about/versions/11/privacy/permissions
- **Purpose**: Permission changes and restrictions in Android 11+
- **Key Changes**: Automatic grants for certain app types, user consent requirements
- **Usage**: Understanding permission model for overlay windows

### 5. canDrawOverlays() Method
- **URL**: https://learn.microsoft.com/en-us/dotnet/api/android.provider.settings.candrawoverlays
- **Purpose**: Check if app has permission to draw overlays
- **Usage**: `Settings.canDrawOverlays(context)` runtime permission check

## Context & Display

### 6. createWindowContext()
- **URL**: https://learn.microsoft.com/en-us/dotnet/api/android.content.context.createwindowcontext
- **Purpose**: Creates a context for non-activity windows (API 30+)
- **Usage**: Proper context for window-based UI elements
- **Parameters**: `Display`, `int type`, `Bundle options`

## Foreground Services

### 7. Foreground Services Guide
- **URL**: https://developer.android.com/develop/background-work/services/fgs
- **Purpose**: Complete guide for implementing foreground services
- **Key Topics**:
  - Service types (Android 14+)
  - Notification requirements
  - Lifecycle management
  - Permission requirements
- **Usage**: Implementing persistent overlay service

## Jetpack Integration

### 8. ComposeView API
- **URL**: https://developer.android.com/reference/kotlin/androidx/compose/ui/platform/ComposeView
- **Purpose**: View for hosting Jetpack Compose UI content
- **Key Methods**: `setContent()`, `setViewCompositionStrategy()`
- **Usage**: Embedding Compose UI in WindowManager overlay

### 9. WindowManager Jetpack Library
- **URL**: https://developer.android.com/jetpack/androidx/releases/window
- **Purpose**: Jetpack library for window features
- **Features**: Foldable support, window metrics
- **Usage**: Enhanced window management capabilities

## Dependency Injection

### 10. Hilt Official Guide
- **URL**: https://developer.android.com/training/dependency-injection/hilt-android
- **Purpose**: Dependency injection framework for Android
- **Key Concepts**:
  - `@HiltAndroidApp`
  - `@AndroidEntryPoint`
  - `@HiltViewModel`
  - Service injection limitations
- **Usage**: Managing dependencies in Service and ViewModel

## Permission Declaration

### 11. Manifest.permission Class
- **URL**: https://developer.android.com/reference/android/Manifest.permission
- **Purpose**: All Android system permissions reference
- **Key Permission**: `SYSTEM_ALERT_WINDOW` - "Allows an app to create windows shown on top of all other apps"
- **Usage**: Understanding permission requirements and declarations

## Practical Examples

### 12. System Alert Window GitHub Example
- **URL**: https://github.com/noln/system-alert-window-example
- **Purpose**: Working example of system alert window implementation
- **Features**: Permission handling, overlay creation, lifecycle management
- **Usage**: Reference implementation patterns

## Implementation Notes

### Current Implementation Status
- Using `TYPE_APPLICATION_OVERLAY` for Android O+ compatibility
- Service-based architecture with `@AndroidEntryPoint`
- ComposeView for UI rendering
- Manual ViewModel creation due to Service limitations

### Known Issues & Solutions
1. **Foreground Service Type** (Android 14+)
   - Must declare `android:foregroundServiceType` in manifest
   - Options: `specialUse`, `microphone`, `mediaProjection`

2. **Hilt ViewModel in Service**
   - Services don't support `@HiltViewModel` injection directly
   - Solution: Inject dependencies and create ViewModel manually

3. **Window Flags Configuration**
   - Critical flags for keyboard-like behavior:
     - `FLAG_NOT_FOCUSABLE` - Don't steal focus
     - `FLAG_NOT_TOUCH_MODAL` - Pass through touches
     - `FLAG_ALT_FOCUSABLE_IM` - Stay above keyboard

### Best Practices
1. Always check `canDrawOverlays()` before showing overlay
2. Handle configuration changes (rotation) properly
3. Clean up WindowManager views in Service.onDestroy()
4. Use appropriate foreground service type for use case
5. Test on various Android versions (API 31+)

## Related EditEcho Files
- `/app/src/main/java/com/editecho/service/OverlayService.kt` - Main overlay service
- `/app/src/main/AndroidManifest.xml` - Permission and service declarations
- `/tasks/prd-keyboard-overlay-enhancement.md` - Product requirements
- `/tasks/overlay-testing-checklist.md` - Testing procedures 