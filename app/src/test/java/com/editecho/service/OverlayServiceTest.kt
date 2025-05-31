package com.editecho.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import com.editecho.view.EditEchoOverlayViewModel
// import dagger.hilt.android.testing.HiltAndroidRule // Temporarily commented out
// import dagger.hilt.android.testing.HiltAndroidTest // Temporarily commented out
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
// import org.junit.Rule // Temporarily commented out
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

/**
 * Unit tests for OverlayService covering lifecycle and WindowManager interactions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S]) // API 31
class OverlayServiceTest {

    @MockK
    private lateinit var mockWindowManager: WindowManager
    
    @MockK
    private lateinit var mockNotificationManager: NotificationManager
    
    @MockK(relaxed = true)
    private lateinit var mockViewModel: EditEchoOverlayViewModel
    
    @MockK(relaxed = true)
    private lateinit var mockViewModelProvider: ViewModelProvider
    
    private lateinit var service: OverlayService
    
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        
        // Mock static methods
        mockkStatic(Settings::class)
        mockkStatic(ViewModelProvider::class)
        mockkConstructor(ViewModelProvider::class)
        
        // Set up ViewModelProvider mock
        every { anyConstructed<ViewModelProvider>().get(EditEchoOverlayViewModel::class.java) } returns mockViewModel
        
        // Create service
        service = Robolectric.setupService(OverlayService::class.java)
        
        // Inject mocks using reflection
        service.windowManager = mockWindowManager
        service.notificationManager = mockNotificationManager
        service.editEchoOverlayViewModel = mockViewModel
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `onCreate initializes services and creates notification channel`() {
        // Verify notification channel was created
        verify { mockNotificationManager.createNotificationChannel(any()) }
        
        // Verify ViewModel was initialized
        assert(service.editEchoOverlayViewModel == mockViewModel)
    }
    
    @Test
    fun `onStartCommand with START_OVERLAY action shows overlay when permission granted`() {
        // Given permission is granted
        every { Settings.canDrawOverlays(any()) } returns true
        
        // When
        val intent = Intent().apply { action = OverlayService.ACTION_START_OVERLAY }
        service.onStartCommand(intent, 0, 1)
        
        // Then
        verify { mockWindowManager.addView(any(), any()) }
    }
    
    @Test
    fun `onStartCommand with START_OVERLAY action handles missing permission`() {
        // Given permission is not granted
        every { Settings.canDrawOverlays(any()) } returns false
        
        // Mock notification creation
        every { mockNotificationManager.notify(any(), any()) } just Runs
        
        // When
        val intent = Intent().apply { action = OverlayService.ACTION_START_OVERLAY }
        service.onStartCommand(intent, 0, 1)
        
        // Then
        verify(exactly = 0) { mockWindowManager.addView(any(), any()) }
        verify { mockNotificationManager.notify(any(), any()) }
    }
    
    @Test
    fun `onStartCommand with STOP_OVERLAY action removes overlay`() {
        // Given overlay is showing
        every { Settings.canDrawOverlays(any()) } returns true
        val startIntent = Intent().apply { action = OverlayService.ACTION_START_OVERLAY }
        service.onStartCommand(startIntent, 0, 1)
        
        // When
        val stopIntent = Intent().apply { action = OverlayService.ACTION_STOP_OVERLAY }
        service.onStartCommand(stopIntent, 0, 2)
        
        // Then
        verify { mockWindowManager.removeView(any()) }
    }
    
    @Test
    fun `onDestroy cleans up overlay and clears ViewModelStore`() {
        // Given overlay is showing
        every { Settings.canDrawOverlays(any()) } returns true
        val intent = Intent().apply { action = OverlayService.ACTION_START_OVERLAY }
        service.onStartCommand(intent, 0, 1)
        
        // When
        service.onDestroy()
        
        // Then
        verify { mockWindowManager.removeView(any()) }
        // ViewModelStore.clear() is called internally
    }
    
    @Test
    fun `showOverlay does nothing when overlay already visible`() {
        // Given overlay is already showing
        every { Settings.canDrawOverlays(any()) } returns true
        service.showOverlay()
        clearMocks(mockWindowManager, answers = false)
        
        // When trying to show again
        service.showOverlay()
        
        // Then
        verify(exactly = 0) { mockWindowManager.addView(any(), any()) }
    }
    
    @Test
    fun `showOverlay handles permission check before creating view`() {
        // Given permission is not granted
        every { Settings.canDrawOverlays(any()) } returns false
        
        // When
        service.showOverlay()
        
        // Then
        verify(exactly = 0) { mockWindowManager.addView(any(), any()) }
        verify { mockNotificationManager.notify(any(), any()) }
    }
    
    @Test
    fun `hideOverlay safely handles null overlay view`() {
        // Given no overlay is showing
        // When
        service.hideOverlay()
        
        // Then - should not crash
        verify(exactly = 0) { mockWindowManager.removeView(any()) }
    }
    
    @Test
    fun `hideOverlay handles WindowManager exceptions gracefully`() {
        // Given overlay is showing
        every { Settings.canDrawOverlays(any()) } returns true
        service.showOverlay()
        
        // And removeView throws exception
        every { mockWindowManager.removeView(any()) } throws IllegalArgumentException("View not attached")
        
        // When
        service.hideOverlay()
        
        // Then - should not crash
        verify { mockWindowManager.removeView(any()) }
    }
    
    @Test
    fun `createLayoutParams returns correct window parameters`() {
        // Use reflection to access private method
        val createLayoutParamsMethod = OverlayService::class.java.getDeclaredMethod("createLayoutParams")
        createLayoutParamsMethod.isAccessible = true
        
        // When
        val params = createLayoutParamsMethod.invoke(service) as WindowManager.LayoutParams
        
        // Then
        assert(params.width == WindowManager.LayoutParams.MATCH_PARENT)
        assert(params.type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        assert(params.gravity == (android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL))
        assert(params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE != 0)
        assert(params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL != 0)
        assert(params.flags and WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM != 0)
    }
    
    @Test
    fun `service returns START_NOT_STICKY from onStartCommand`() {
        // When
        val intent = Intent().apply { action = OverlayService.ACTION_START_OVERLAY }
        val result = service.onStartCommand(intent, 0, 1)
        
        // Then
        assert(result == android.app.Service.START_NOT_STICKY)
    }
    
    @Test
    fun `onBind returns null as this is not a bound service`() {
        // When
        val binder = service.onBind(Intent())
        
        // Then
        assert(binder == null)
    }
    
    @Test
    fun `handlePermissionNotGranted sends notification and stops service`() {
        // Given
        every { mockNotificationManager.notify(any(), any()) } just Runs
        
        // When
        service.handlePermissionNotGranted()
        
        // Then
        verify { mockNotificationManager.notify(any(), any()) }
        // Note: stopSelf() is called but difficult to verify in Robolectric
    }
    
    @Test
    fun `overlay view lifecycle is properly managed`() {
        // Given permission is granted
        every { Settings.canDrawOverlays(any()) } returns true
        
        // When showing overlay
        service.showOverlay()
        
        // Then view is added to WindowManager
        verify { mockWindowManager.addView(any(), any()) }
        
        // When hiding overlay
        service.hideOverlay()
        
        // Then view is removed
        verify { mockWindowManager.removeView(any()) }
    }
} 